/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseThreads;
import com.s3auth.hosts.Hosts;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import javax.net.ssl.SSLServerSocketFactory;
import javax.validation.constraints.NotNull;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 * All protocol-neutral machinery (executor services, socket queue, accept
 * loop, graceful shutdown) lives in {@link Facade}; this class only
 * provides FTP-specific behavior.</p>
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @see Main
 * @see Facade
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class FtpFacade implements Closeable {

    /**
     * The protocol-neutral facade doing all the work.
     */
    private final transient Facade facade;

    /**
     * Private ctor.
     *
     * @param fcd The protocol-neutral facade
     */
    private FtpFacade(final Facade fcd) {
        this.facade = fcd;
    }

    @Override
    public void close() throws IOException {
        this.facade.close();
    }

    /**
     * Start listening to the ports.
     */
    void listen() {
        this.facade.listen();
    }

    /**
     * Open a facade and start its backend threads.
     *
     * <p>The plain port is bound first, then the secured one. If the
     * secured bind fails, the already-bound plain socket is closed
     * before the exception propagates, so a failed initialization never
     * leaks the plain port.</p>
     *
     * @param hosts Hosts
     * @param port Port number
     * @param sslport SSL port number
     * @return Opened facade
     * @throws IOException If can't initialize
     */
    @SuppressWarnings("PMD.CloseResource")
    static FtpFacade open(@NotNull final Hosts hosts, final int port,
        final int sslport) throws IOException {
        final BlockingQueue<Socket> sockets = new SynchronousQueue<>();
        final ServerSocket plain = new ServerSocket(port);
        final ServerSocket secured;
        try {
            secured = SSLServerSocketFactory.getDefault()
                .createServerSocket(sslport);
        } catch (final IOException ex) {
            try {
                plain.close();
            } catch (final IOException failure) {
                ex.addSuppressed(failure);
            }
            throw ex;
        }
        final Facade facade = FtpFacade.assemble(
            hosts, sockets, Arrays.asList(plain, secured)
        );
        facade.start();
        return new FtpFacade(facade);
    }

    private static Facade assemble(@NotNull final Hosts hosts,
        final BlockingQueue<Socket> sockets,
        final Collection<ServerSocket> servers) {
        return new Facade(
            Executors.newScheduledThreadPool(
                2, new VerboseThreads("FTP-front")
            ),
            Executors.newScheduledThreadPool(
                Facade.THREADS, new VerboseThreads("FTP-back")
            ),
            sockets,
            servers,
            new FtpThreadRunnable(new FtpThread(sockets, hosts)),
            FtpFacade::overflow,
            10_000L,
            10_000L
        );
    }

    private static void overflow(final Socket socket) {
        new FtpResponse()
            .withCode(FTPReply.SERVICE_NOT_AVAILABLE).withText(
                String.format(
                    "We're sorry, the service is under high load at the moment (%d open connections), please try again in a few minutes",
                    Facade.THREADS
                )
            )
            .send(socket);
    }
}
