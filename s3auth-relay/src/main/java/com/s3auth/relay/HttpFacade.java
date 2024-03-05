/*
 * Copyright (c) 2012-2024, Yegor Bugayenko
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
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.s3auth.hosts.Hosts;
import java.io.Closeable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLServerSocketFactory;
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
 * @since 0.0.1
 * @see Main
 * @todo #213:1hr Create new a class Facade with all protocol-neutral code
 *  (such as socket handling, socket queue, etc). Then convert {@link com
 *  .s3auth.relay.HttpFacade} and {@link com.s3auth.relay.FtpFacade} so they use
 *  the new Facade class in order to avoid code duplication.
 */
@ToString
@EqualsAndHashCode(of = { "sockets", "server" })
@SuppressWarnings("PMD.DoNotUseThreads")
@Loggable(Loggable.DEBUG)
final class HttpFacade implements Closeable {

    /**
     * How many threads to use.
     */
    private static final int THREADS = 100;

    /**
     * Executor service, with socket openers.
     */
    private final transient ScheduledExecutorService frontend;

    /**
     * Executor service, with consuming threads.
     */
    private final transient ScheduledExecutorService backend;

    /**
     * Blocking queue of ready-to-be-processed sockets.
     */
    private final transient BlockingQueue<Socket> sockets;

    /**
     * Server socket.
     */
    private final transient ServerSocket server;

    /**
     * Secured Server socket.
     */
    private final transient ServerSocket secured;

    /**
     * Public ctor.
     * @param hosts Hosts
     * @param port Port number
     * @param sslport SSL port number.
     * @throws IOException If can't initialize
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    HttpFacade(@NotNull final Hosts hosts, final int port, final int sslport)
        throws IOException {
        this.frontend = Executors.newScheduledThreadPool(2, new VerboseThreads("front"));
        this.server = new ServerSocket(port);
        this.backend = Executors.newScheduledThreadPool(
            HttpFacade.THREADS,
            new VerboseThreads("back")
        );
        this.sockets = new SynchronousQueue<>();
        this.secured = SSLServerSocketFactory.getDefault()
            .createServerSocket(sslport);
        final Runnable runnable = new VerboseRunnable(
            new HttpFacade.HttpThreadRunnable(
                new HttpThread(this.sockets, hosts)
            ),
            true, true
        );
        for (int idx = 0; idx < HttpFacade.THREADS; ++idx) {
            this.backend.scheduleWithFixedDelay(
                runnable,
                0L, 1L, TimeUnit.NANOSECONDS
            );
        }
    }

    /**
     * Start listening to the ports.
     */
    public void listen() {
        this.frontend.scheduleWithFixedDelay(
            new VerboseRunnable(
                () -> this.process(this.server)
            ),
            0L, 1L, TimeUnit.NANOSECONDS
        );
        this.frontend.scheduleWithFixedDelay(
            new VerboseRunnable(
                () -> this.process(this.secured)
            ),
            0L, 1L, TimeUnit.NANOSECONDS
        );
    }

    @Override
    public void close() throws IOException {
        try {
            this.shutdown(this.frontend);
            this.shutdown(this.backend);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        this.server.close();
    }

    /**
     * Process one server socket.
     * @param svr The server socket
     */
    private void process(final ServerSocket svr) {
        final Socket socket;
        try {
            socket = svr.accept();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            if (!this.sockets.offer(socket, 10, TimeUnit.SECONDS)) {
                HttpFacade.overflow(socket);
                Logger.warn(
                    this, "too many open connections (%d), can't open any more",
                    HttpFacade.THREADS
                );
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Report overflow problem to the socket and close it.
     * @param socket The socket to report to
     */
    private static void overflow(final Socket socket) {
        try {
            new HttpResponse()
                .withStatus(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .withBody(
                    String.format(
                        // @checkstyle LineLength (1 line)
                        "We're sorry, the site is under high load at the moment (%d open connections), please try again in a few minutes",
                        HttpFacade.THREADS
                    )
                )
                .send(socket);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Shutdown a service.
     * @param service The service to shut down
     * @throws InterruptedException If fails to shutdown
     */
    private void shutdown(final ExecutorService service)
        throws InterruptedException {
        service.shutdown();
        if (service.awaitTermination(100, TimeUnit.MILLISECONDS)) {
            Logger.info(this, "#shutdown(): succeeded");
        } else {
            Logger.warn(this, "#shutdown(): failed");
            service.shutdownNow();
            if (service.awaitTermination(1, TimeUnit.SECONDS)) {
                Logger.info(this, "#shutdown(): shutdownNow() succeeded");
            } else {
                Logger.error(this, "#shutdown(): failed to stop threads");
            }
        }
    }

    /**
     * Dispatcher of HttpThread.
     *
     * @since 0.0.1
     */
    private static final class HttpThreadRunnable implements Runnable {
        /**
         * The thread to run.
         */
        private final transient HttpThread thread;

        /**
         * Constructor.
         * @param thrd The HttpThread
         */
        HttpThreadRunnable(final HttpThread thrd) {
            this.thread = thrd;
        }

        @Override
        public void run() {
            try {
                this.thread.dispatch();
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
                Logger.warn(this, "%s", ex);
            }
        }
    }
}
