/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.google.common.collect.ImmutableSet;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.GzipResource;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.Version;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.DateUtils;

/**
 * Single HTTP processing thread.
 *
 * <p>The class is responsible for getting a new socket from a blocking
 * queue, processing it, and closing the socket. The class is instantiated
 * by {@link HttpFacade} and is executed by Services Executor routinely.
 *
 * <p>The class is thread-safe.
 *
 * @since 0.0.1
 * @see HttpFacade
 */
@ToString
@EqualsAndHashCode(of = { "hosts", "sockets" })
@SuppressWarnings({
    "PMD.DoNotUseThreads",
    "PMD.UseConcurrentHashMap",
    "PMD.CyclomaticComplexity"
})
final class HttpThread {

    /**
     * S3 version query string.
     */
    private static final String VER = "ver";

    /**
     * S3 version listing query string.
     */
    private static final String ALL_VERSIONS = "all-versions";

    /**
     * Name of the server we show in HTTP headers.
     */
    private static final String NAME = String.format(
        "relay.s3auth.com, %s/%s built on %s",
        Manifests.read("S3Auth-Version"),
        Manifests.read("S3Auth-Revision"),
        Manifests.read("S3Auth-Date")
    );

    /**
     * Compressible content types.
     */
    private static final Collection<String> COMPRESSIBLE =
        ImmutableSet.<String>builder()
            .add("text/plain")
            .add("text/html")
            .add("text/xml")
            .add("text/css")
            .add("application/xml")
            .add("application/xhtml")
            .add("application/xhtml+xml")
            .add("application/rss+xml")
            .add("application/javascript")
            .add("application/x-javascript")
            .build();

    /**
     * Queue of sockets to get from.
     */
    private final transient BlockingQueue<Socket> sockets;

    /**
     * Hosts to work with.
     */
    private final transient Hosts hosts;

    /**
     * Public ctor.
     * @param sckts Sockets to read from
     * @param hsts Hosts
     */
    HttpThread(@NotNull final BlockingQueue<Socket> sckts,
        @NotNull final Hosts hsts) {
        this.sockets = sckts;
        this.hosts = hsts;
    }

    /**
     * Dispatch one request from the encapsulated queue.
     * @return Amount of bytes sent to socket
     * @throws InterruptedException If interrupted while waiting for the queue
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public long dispatch() throws InterruptedException {
        final Socket socket = this.sockets.take();
        final long start = System.currentTimeMillis();
        long bytes = 0L;
        try {
            final HttpRequest request = new HttpRequest(socket);
            final boolean get = "GET".equals(request.method());
            if (get || "HEAD".equals(request.method())) {
                HttpResponse response = new HttpResponse()
                    .withHeader("Server", HttpThread.NAME)
                    .withHeader(
                        HttpHeaders.DATE,
                        String.format(
                            "%ta, %1$td %1$tb %1$tY %1$tT %1$tz",
                            new Date()
                        )
                    )
                    .withHeader(
                        "X-S3auth-Time",
                        Long.toString(System.currentTimeMillis() - start)
                    );
                Resource resource = null;
                try {
                    resource = HttpThread.resource(this.host(request), request);
                    response = response.withHeader(
                        org.apache.http.HttpHeaders.AGE,
                        String.valueOf(
                            TimeUnit.MILLISECONDS.toSeconds(
                                System.currentTimeMillis() - start
                            )
                        )
                    );
                    if (resource.lastModified() != null) {
                        response = response.withHeader(
                            HttpHeaders.LAST_MODIFIED,
                            DateUtils.formatDate(resource.lastModified())
                        );
                    }
                    if (get) {
                        response = response.withBody(resource);
                    }
                    bytes = response.send(socket);
                    Logger.info(
                        this, "#dispath(): %d bytes of %s", bytes, resource
                    );
                } finally {
                    if (resource != null) {
                        resource.close();
                    }
                }
            } else {
                bytes = this.failure(
                    new HttpException(
                        HttpURLConnection.HTTP_BAD_METHOD,
                        "only GET and HEAD methods are supported at the moment"
                    ),
                    socket
                );
            }
        } catch (final HttpException ex) {
            Logger.info(this, "#dispatch(): %[exception]s", ex);
            bytes = this.failure(ex, socket);
        } catch (final SocketException ex) {
            Logger.info(this, "#dispatch(): %[exception]s", ex);
            bytes = 0L;
        // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            Logger.info(this, "#dispatch(): %[exception]s", ex);
            bytes = this.failure(
                new HttpException(
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    ex
                ),
                socket
            );
        } finally {
            IOUtils.closeQuietly(socket);
        }
        return bytes;
    }

    /**
     * Make a resource from host and request.
     * @param host The host
     * @param request HTTP request
     * @return The resource
     * @throws IOException If some IO exception
     */
    private static Resource resource(final Host host, final HttpRequest request)
        throws IOException {
        final Version version;
        if (request.parameters().containsKey(HttpThread.ALL_VERSIONS)) {
            version = Version.LIST;
        } else if (request.parameters().containsKey(HttpThread.VER)) {
            version = new Version.Simple(
                request.parameters().get(HttpThread.VER)
                    .iterator().next()
            );
        } else {
            version = Version.LATEST;
        }
        Resource resource = host.fetch(
            request.requestUri(), request.range(), version
        );
        if (request.headers().containsKey(HttpHeaders.IF_NONE_MATCH)) {
            final String etag = request.headers()
                .get(HttpHeaders.IF_NONE_MATCH)
                .iterator().next();
            if (etag.equals(resource.etag())) {
                throw new HttpException(HttpURLConnection.HTTP_NOT_MODIFIED);
            }
        }
        if (request.headers().containsKey(HttpHeaders.IF_MODIFIED_SINCE)) {
            final Date since = DateUtils.parseDate(
                request.headers().get(HttpHeaders.IF_MODIFIED_SINCE)
                    .iterator().next()
            );
            if (resource.lastModified().before(since)) {
                throw new HttpException(HttpURLConnection.HTTP_NOT_MODIFIED);
            }
        }
        if (request.headers().containsKey(HttpHeaders.ACCEPT_ENCODING)
            && request.headers().get(HttpHeaders.ACCEPT_ENCODING)
                .contains("gzip")
            && HttpThread.COMPRESSIBLE.contains(resource.contentType())) {
            resource = new GzipResource(resource);
        }
        return resource;
    }

    /**
     * Get host from request.
     * @param request The HTTP request
     * @return Host ready to fetch content
     * @throws HttpException If some error inside
     */
    private Host host(final HttpRequest request) throws HttpException {
        final Map<String, Collection<String>> headers = request.headers();
        if (!headers.containsKey(HttpHeaders.HOST)) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                String.format(
                    "'%s' HTTP header missed",
                    HttpHeaders.HOST
                )
            );
        }
        if (headers.get(HttpHeaders.HOST).size() != 1) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                String.format(
                    "only one '%s' HTTP header allowed",
                    HttpHeaders.HOST
                )
            );
        }
        final String domain = headers.get(HttpHeaders.HOST).iterator().next();
        final Host host;
        if (LocalHost.isIt(domain)) {
            host = new LocalHost();
        } else {
            try {
                host = new SecuredHost(this.hosts.find(domain), request);
            } catch (final Hosts.NotFoundException ex) {
                throw new HttpException(
                    HttpURLConnection.HTTP_NOT_FOUND,
                    ex
                );
            } catch (final IOException ex) {
                throw new HttpException(
                    HttpURLConnection.HTTP_INTERNAL_ERROR,
                    ex
                );
            }
        }
        return host;
    }

    /**
     * Send failure to the socket.
     * @param cause The problem
     * @param socket The socket to talk to
     * @return Number of bytes sent
     */
    private long failure(final HttpException cause,
        final Socket socket) {
        try {
            final long bytes = cause.response().send(socket);
            Logger.info(this, "#run(): failure sent to %s", socket);
            return bytes;
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
