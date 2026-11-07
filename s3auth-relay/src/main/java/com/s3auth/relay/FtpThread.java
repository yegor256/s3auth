/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.log.Logger;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.Version;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Single FTP processing thread.
 *
 * <p>The class is responsible for getting a new socket from a blocking
 * queue, greeting the client, and processing its FTP commands on the
 * control connection until it is closed. The USER command carries the
 * domain name of the host to work with, found in {@link Hosts}. The PASS
 * command carries the credentials in the {@code "user:password"} format,
 * the same pair as in HTTP Basic Auth. The RETR command fetches the
 * resource from the host and relays it over the data connection,
 * previously negotiated by the PORT command. All other commands get an
 * error reply, they are unsupported for now. The class is instantiated
 * by {@link FtpFacade} and is executed by Services Executor routinely.</p>
 *
 * <p>The class is thread-safe.</p>
 *
 * @see FtpFacade
 * @since 0.0.1
 * @todo #565:30min Support passive mode (PASV) data connections, which
 *  many FTP clients prefer over the active mode implemented here with
 *  the PORT command.
 */
final class FtpThread {

    /**
     * Number of octets in the PORT command argument.
     */
    private static final int PORT_OCTETS = 6;

    /**
     * Pattern to split an FTP command line into command and argument.
     */
    private static final Pattern COMMAND_LINE = Pattern.compile("\\s+");

    /**
     * Pattern to split the PORT command argument into octets.
     */
    private static final Pattern COMMA = Pattern.compile(",");

    /**
     * Queue of sockets to get from.
     */
    @NotNull
    private final transient BlockingQueue<Socket> sockets;

    /**
     * Hosts to work with.
     */
    @NotNull
    private final transient Hosts hosts;

    /**
     * Public ctor.
     *
     * @param sckts Sockets to read from
     * @param hsts Hosts
     */
    FtpThread(@NotNull final BlockingQueue<Socket> sckts,
        @NotNull final Hosts hsts) {
        this.sockets = sckts;
        this.hosts = hsts;
    }

    /**
     * Dispatch one FTP session: take a socket from the encapsulated
     * queue, greet the client, and process its commands until QUIT or
     * until the client closes the connection.
     *
     * @return Amount of bytes sent to the client
     * @throws InterruptedException If interrupted while waiting for the queue
     */
    @SuppressWarnings({
        "PMD.AvoidCatchingGenericException",
        "PMD.CloseResource",
        "PMD.UseTryWithResources"
    })
    long dispatch() throws InterruptedException {
        final Socket socket = this.sockets.take();
        long bytes;
        try {
            bytes = this.session(socket);
        } catch (final SocketException ex) {
            Logger.info(this, "#dispatch(): %[exception]s", ex);
            bytes = 0L;
        // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Logger.warn(this, "#dispatch(): %[exception]s", ex);
            bytes = 0L;
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return bytes;
    }

    private long session(final Socket socket) throws IOException {
        long bytes = new FtpResponse().withCode(
            FTPReply.SERVICE_READY
        ).withText("s3auth FTP relay ready").send(socket);
        Host host = null;
        InetSocketAddress data = null;
        boolean auth = false;
        boolean quit = false;
        try (
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream(), StandardCharsets.UTF_8
                )
            )
        ) {
            while (!quit) {
                final String line = reader.readLine();
                if (line == null) {
                    break;
                }
                final String[] parts = FtpThread.COMMAND_LINE.split(
                    line.trim(), 2
                );
                final String cmd = parts[0].toUpperCase(Locale.ENGLISH);
                try {
                    if ("USER".equals(cmd)) {
                        host = this.user(parts, socket);
                        auth = false;
                    } else if ("PASS".equals(cmd)) {
                        auth = this.pass(host, parts, socket);
                    } else if ("PORT".equals(cmd)) {
                        data = FtpThread.port(parts, socket);
                    } else if ("RETR".equals(cmd)) {
                        bytes += this.retr(host, auth, data, parts, socket);
                    } else if ("QUIT".equals(cmd)) {
                        bytes += FtpThread.closing(socket);
                        quit = true;
                    } else {
                        bytes += FtpThread.unsupported(cmd, socket);
                    }
                } catch (final FtpException ex) {
                    bytes += ex.response().send(socket);
                }
            }
        }
        return bytes;
    }

    private Host user(final String[] parts, final Socket socket)
        throws IOException {
        final String domain = FtpThread.argument(parts);
        final Host host;
        try {
            host = this.hosts.find(domain);
        } catch (final Hosts.NotFoundException ex) {
            throw new FtpException(
                FTPReply.NOT_LOGGED_IN,
                String.format("unknown domain '%s'", domain),
                ex
            );
        } catch (final IOException ex) {
            Logger.warn(this, "#user(): %[exception]s", ex);
            throw new FtpException(
                FTPReply.SERVICE_NOT_AVAILABLE,
                "failed to find the host",
                ex
            );
        }
        new FtpResponse().withCode(
            FTPReply.NEED_PASSWORD
        ).withText(
            String.format("password required for '%s'", domain)
        ).send(socket);
        return host;
    }

    private boolean pass(final Host host, final String[] parts,
        final Socket socket) throws IOException {
        if (host == null) {
            throw new FtpException(
                FTPReply.NOT_LOGGED_IN, "send USER command first"
            );
        }
        final String[] creds = FtpThread.argument(parts).split(":", 2);
        if (creds.length != 2) {
            throw new FtpException(
                FTPReply.NOT_LOGGED_IN,
                "credentials should be in 'user:password' format"
            );
        }
        final boolean authorized;
        try {
            authorized = host.authorized(creds[0], creds[1]);
        } catch (final IOException ex) {
            Logger.warn(this, "#pass(): %[exception]s", ex);
            throw new FtpException(
                FTPReply.SERVICE_NOT_AVAILABLE,
                "failed to check credentials",
                ex
            );
        }
        if (!authorized) {
            throw new FtpException(FTPReply.NOT_LOGGED_IN, "login incorrect");
        }
        new FtpResponse().withCode(
            FTPReply.USER_LOGGED_IN
        ).withText("user logged in").send(socket);
        return true;
    }

    private static InetSocketAddress port(final String[] parts,
        final Socket socket) throws IOException {
        final String[] octets = FtpThread.COMMA.split(
            FtpThread.argument(parts), -1
        );
        if (octets.length != FtpThread.PORT_OCTETS) {
            throw new FtpException(
                FTPReply.SYNTAX_ERROR_IN_ARGUMENTS,
                String.format(
                    "PORT argument should have %d octets",
                    FtpThread.PORT_OCTETS
                )
            );
        }
        final int[] values = new int[FtpThread.PORT_OCTETS];
        for (int idx = 0; idx < FtpThread.PORT_OCTETS; ++idx) {
            values[idx] = FtpThread.octet(octets[idx].trim());
        }
        final InetSocketAddress data = new InetSocketAddress(
            String.format(
                "%d.%d.%d.%d", values[0], values[1], values[2], values[3]
            ),
            (values[4] << 8) + values[5]
        );
        new FtpResponse().withCode(
            FTPReply.COMMAND_OK
        ).withText("PORT command successful").send(socket);
        return data;
    }

    private long retr(final Host host, final boolean auth,
        final InetSocketAddress data, final String[] parts,
        final Socket socket) throws IOException {
        final String path = FtpThread.argument(parts);
        if (!auth) {
            throw new FtpException(
                FTPReply.NOT_LOGGED_IN, "login with USER and PASS first"
            );
        }
        if (data == null) {
            throw new FtpException(
                FTPReply.CANNOT_OPEN_DATA_CONNECTION,
                "send PORT command first"
            );
        }
        final Resource resource;
        try {
            resource = host.fetch(
                URI.create(path), Range.ENTIRE, Version.LATEST
            );
        } catch (final IOException ex) {
            Logger.warn(this, "#retr(): %[exception]s", ex);
            throw new FtpException(
                FTPReply.FILE_UNAVAILABLE,
                String.format("failed to fetch '%s'", path),
                ex
            );
        }
        long bytes;
        try (Resource res = resource) {
            if (res.status() != HttpURLConnection.HTTP_OK) {
                throw new FtpException(
                    FTPReply.FILE_UNAVAILABLE,
                    String.format("status %d for '%s'", res.status(), path)
                );
            }
            bytes = new FtpResponse()
                .withCode(FTPReply.FILE_STATUS_OK).withText(
                    String.format("opening data connection for '%s'", path)
                ).send(socket);
            bytes += this.transfer(res, data);
            bytes += new FtpResponse().withCode(
                FTPReply.CLOSING_DATA_CONNECTION
            ).withText("transfer complete").send(socket);
        }
        return bytes;
    }

    @SuppressWarnings("PMD.CloseResource")
    private long transfer(final Resource resource,
        final InetSocketAddress data) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(data);
            final OutputStream stream = socket.getOutputStream();
            final long bytes = resource.writeTo(stream);
            stream.flush();
            return bytes;
        } catch (final IOException ex) {
            Logger.warn(this, "#transfer(): %[exception]s", ex);
            throw new FtpException(
                FTPReply.TRANSFER_ABORTED,
                "failed to send the resource over the data connection",
                ex
            );
        }
    }

    private static long closing(final Socket socket) throws IOException {
        return new FtpResponse().withCode(
            FTPReply.SERVICE_CLOSING_CONTROL_CONNECTION
        ).withText("bye").send(socket);
    }

    private static long unsupported(final String cmd, final Socket socket)
        throws IOException {
        return new FtpResponse().withCode(
            FTPReply.COMMAND_NOT_IMPLEMENTED
        ).withText(String.format("'%s' is not supported", cmd)).send(socket);
    }

    private static int octet(final String value) throws IOException {
        final int octet;
        try {
            octet = Integer.parseInt(value);
        } catch (final NumberFormatException ex) {
            throw new FtpException(
                FTPReply.SYNTAX_ERROR_IN_ARGUMENTS,
                String.format("'%s' is not a valid octet", value),
                ex
            );
        }
        if (octet < 0 || octet > 255) {
            throw new FtpException(
                FTPReply.SYNTAX_ERROR_IN_ARGUMENTS,
                String.format("'%s' is out of range 0..255", value)
            );
        }
        return octet;
    }

    private static String argument(final String... parts) {
        final String arg;
        if (parts.length > 1) {
            arg = parts[1].trim();
        } else {
            arg = "";
        }
        return arg;
    }
}
