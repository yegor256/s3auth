/**
 * Copyright (c) 2012, s3auth.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the s3auth.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @see HttpFacade
 */
@ToString
@EqualsAndHashCode(of = { "hosts", "sockets" })
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.UseConcurrentHashMap" })
@Loggable(Loggable.DEBUG)
final class HttpThread {

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
     */
    @Loggable(value = Loggable.DEBUG, limit = Integer.MAX_VALUE)
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public long dispatch() throws InterruptedException {
        final Socket socket = this.sockets.take();
        final long start = System.currentTimeMillis();
        long bytes;
        try {
            final HttpRequest request = new HttpRequest(socket);
            if ("GET".equals(request.method())) {
                bytes = new HttpResponse()
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
                    )
                    .withBody(this.resource(this.host(request), request))
                    .send(socket);
            } else {
                bytes = HttpThread.failure(
                    new HttpException(
                        HttpURLConnection.HTTP_BAD_METHOD,
                        "only GET method is supported at the moment"
                    ),
                    socket
                );
            }
        } catch (final HttpException ex) {
            bytes = HttpThread.failure(ex, socket);
        } catch (final SocketException ex) {
            Logger.warn(this, "#run(): %s", ex);
            bytes = 0L;
        // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            bytes = HttpThread.failure(
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
    private Resource resource(final Host host, final HttpRequest request)
        throws IOException {
        final Resource resource = host.fetch(
            request.requestUri(), request.range()
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
    private static long failure(final HttpException cause,
        final Socket socket) {
        try {
            return cause.response().send(socket);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
