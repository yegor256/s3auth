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
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.s3auth.hosts.Hosts;
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * HTTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @see Main
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = { "sockets", "server" })
@SuppressWarnings("PMD.DoNotUseThreads")
@Loggable(Loggable.DEBUG)
final class HttpFacade implements Closeable {

    /**
     * How many threads to use.
     */
    private static final int THREADS =
        Runtime.getRuntime().availableProcessors() * Tv.EIGHT;

    /**
     * Executor service, with socket openers.
     */
    private final transient ScheduledExecutorService frontend =
        Executors.newSingleThreadScheduledExecutor(new VerboseThreads("front"));

    /**
     * Executor service, with consuming threads.
     */
    private final transient ScheduledExecutorService backend =
        Executors.newScheduledThreadPool(
            HttpFacade.THREADS,
            new VerboseThreads("back")
        );

    /**
     * Blocking queue of ready-to-be-processed sockets.
     */
    private final transient BlockingQueue<Socket> sockets =
        new SynchronousQueue<Socket>();

    /**
     * Server socket.
     */
    private final transient ServerSocket server;

    /**
     * Public ctor.
     * @param hosts Hosts
     * @param port Port number
     * @throws IOException If can't initialize
     */
    HttpFacade(@NotNull final Hosts hosts, final int port)
        throws IOException {
        this.server = new ServerSocket(port);
        final HttpThread thread = new HttpThread(this.sockets, hosts);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    thread.dispatch();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Logger.warn(this, "%s", ex);
                }
            }
        };
        for (int idx = 0; idx < HttpFacade.THREADS; ++idx) {
            this.backend.scheduleWithFixedDelay(
                runnable,
                0L, 1L, TimeUnit.NANOSECONDS
            );
        }
    }

    /**
     * Start listening to the port.
     */
    public void listen() {
        this.frontend.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        HttpFacade.this.process();
                    }
                }
            ),
            0L, 1L, TimeUnit.NANOSECONDS
        );
    }

    @Override
    public void close() throws IOException {
        try {
            this.shutdown(this.frontend);
            this.shutdown(this.backend);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        this.server.close();
    }

    /**
     * Process one socket.
     */
    private void process() {
        final Socket socket;
        try {
            socket = this.server.accept();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            if (!this.sockets.offer(socket, 1L, TimeUnit.SECONDS)) {
                this.overflow(socket);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Report overflow problem to the socket and close it.
     * @param socket The socket to report to
     */
    private void overflow(final Socket socket) {
        try {
            new HttpResponse()
                .withStatus(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .withBody(
                    String.format(
                        "%d socket(s) in queue, %d remaining capacity",
                        this.sockets.size(),
                        this.sockets.remainingCapacity()
                    )
                )
                .send(socket);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Shutdown a service.
     * @param service The service to shut down
     * @throws InterruptedException If fails to shutdown
     */
    private void shutdown(final ScheduledExecutorService service)
        throws InterruptedException {
        service.shutdown();
        if (service.awaitTermination(2L, TimeUnit.SECONDS)) {
            Logger.info(this, "#shutdown(): succeeded");
        } else {
            Logger.warn(this, "#shutdown(): failed");
            service.shutdownNow();
            if (service.awaitTermination(2L, TimeUnit.SECONDS)) {
                Logger.info(this, "#shutdown(): shutdownNow() succeeded");
            } else {
                Logger.error(this, "#shutdown(): failed to stop threads");
            }
        }
    }

}
