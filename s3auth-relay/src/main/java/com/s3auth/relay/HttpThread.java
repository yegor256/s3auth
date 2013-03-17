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
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;

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
@SuppressWarnings("PMD.DoNotUseThreads")
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
    public HttpThread(@NotNull final BlockingQueue<Socket> sckts,
        @NotNull final Hosts hsts) {
        this.sockets = sckts;
        this.hosts = hsts;
    }

    /**
     * Dispatch one request from the encapsulated queue.
     * @return Amount of bytes sent to socket
     * @throws InterruptedException If interrupted while waiting for the queue
     */
    public long dispatch() throws InterruptedException {
        final Socket socket = this.sockets.take();
        final long start = System.currentTimeMillis();
        long bytes;
        try {
            final HttpRequest request = new HttpRequest(socket);
            final Host host = this.host(request);
            final Resource resource = host.fetch(
                request.requestUri(), request.range()
            );
            bytes = new HttpResponse()
                .withStatus(HttpURLConnection.HTTP_OK)
                .withHeader(HttpHeaders.CACHE_CONTROL, "no-cache")
                .withHeader(HttpHeaders.EXPIRES, "-1")
                .withHeader(
                    HttpHeaders.DATE,
                    String.format(
                        "%ta, %1$td %1$tb %1$tY %1$tT %1$tz",
                        new Date()
                    )
                )
                .withHeader("Server", HttpThread.NAME)
                .withHeader(
                    "X-S3auth-Time",
                    Long.toString(System.currentTimeMillis() - start)
                )
                .withBody(resource)
                .send(socket);
            Logger.info(
                this,
                "#run(): %s/%s sent %d bytes to %s in %[ms]s",
                host,
                resource,
                bytes,
                socket.getRemoteSocketAddress(),
                System.currentTimeMillis() - start
            );
        } catch (HttpException ex) {
            bytes = this.failure(ex, socket);
        } catch (java.io.IOException ex) {
            Logger.warn(this, "#run(): IO problem: %s", ex.getMessage());
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
     * Get host from request.
     * @param request The HTTP request
     * @return Host ready to fetch content
     * @throws HttpException If some error inside
     */
    private Host host(final HttpRequest request) throws HttpException {
        final ConcurrentMap<String, Collection<String>> headers =
            request.headers();
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
        Host host;
        if ("relay.s3auth.com".equals(domain)) {
            host = new LocalHost();
        } else {
            try {
                host = new SecuredHost(this.hosts.find(domain), request);
            } catch (Hosts.NotFoundException ex) {
                throw new HttpException(
                    HttpURLConnection.HTTP_NOT_FOUND,
                    ex
                );
            } catch (java.io.IOException ex) {
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
    private long failure(final HttpException cause, final Socket socket) {
        try {
            return cause.response().send(socket);
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
