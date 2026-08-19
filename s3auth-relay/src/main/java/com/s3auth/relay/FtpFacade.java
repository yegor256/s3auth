/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import com.jcabi.log.VerboseThreads;
import com.s3auth.hosts.Hosts;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @see Main
 * @since 0.0.1
 * @todo #213:30min Implement TLS secure port listening in a manner analogous to
 *  HttpFacade.
 */
@Loggable(Loggable.DEBUG)
final class FtpFacade implements Closeable {

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
     * Private ctor, threads started by {@link #open}.
     * @param frnt Frontend executor
     * @param back Backend executor
     * @param skts Blocking queue of ready-to-be-processed sockets
     * @param srv Server socket
     */
    private FtpFacade(final ScheduledExecutorService frnt,
        final ScheduledExecutorService back,
        final BlockingQueue<Socket> skts, final ServerSocket srv) {
        this.frontend = frnt;
        this.backend = back;
        this.sockets = skts;
        this.server = srv;
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
     * Start listening to the ports.
     */
    void listen() {
        Logger.debug(
            this, "#listen(): scheduled %s",
            this.frontend.scheduleWithFixedDelay(
                new VerboseRunnable(() -> this.process(this.server)),
                0L, 1L, TimeUnit.NANOSECONDS
            )
        );
    }

    /**
     * Open a facade and start its backend threads.
     * @param hosts Hosts
     * @param port Port number
     * @return Opened facade
     * @throws IOException If can't initialize
     */
    static FtpFacade open(@NotNull final Hosts hosts, final int port)
        throws IOException {
        final FtpFacade facade = new FtpFacade(
            Executors.newScheduledThreadPool(2, new VerboseThreads("FTP-front")),
            Executors.newScheduledThreadPool(FtpFacade.THREADS, new VerboseThreads("FTP-back")),
            new SynchronousQueue<>(),
            new ServerSocket(port)
        );
        facade.start(hosts);
        return facade;
    }

    /**
     * Start the backend dispatcher threads.
     * @param hosts Hosts
     */
    private void start(final Hosts hosts) {
        final Runnable runnable = new VerboseRunnable(
            new FtpFacade.FtpThreadRunnable(new FtpThread(this.sockets, hosts)), true, true
        );
        for (int idx = 0; idx < FtpFacade.THREADS; ++idx) {
            final ScheduledFuture<?> future = this.backend.scheduleWithFixedDelay(
                runnable, 0L, 1L, TimeUnit.NANOSECONDS
            );
            Logger.debug(this, "#start(): scheduled %s", future);
        }
    }

    /**
     * Process one server socket.
     *
     * <p>Socket ownership transfers to the queue; {@link FtpThread} closes
     * it later.
     *
     * @param svr The server socket
     */
    @SuppressWarnings("PMD.CloseResource")
    private void process(final ServerSocket svr) {
        final Socket socket;
        try {
            socket = svr.accept();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            if (!this.sockets.offer(socket, 10, TimeUnit.SECONDS)) {
                FtpFacade.overflow(socket);
                Logger.warn(this, "too many open connections");
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
        new FtpResponse()
            .withCode(FTPReply.SERVICE_NOT_AVAILABLE).withText(
                String.format(
                    "We're sorry, the service is under high load at the moment (%d open connections), please try again in a few minutes",
                    FtpFacade.THREADS
                )
            )
            .send(socket);
    }

    /**
     * Shutdown a service.
     * @param service The service to shut down
     * @throws InterruptedException If fails to shutdown
     */
    private void shutdown(final ExecutorService service)
        throws InterruptedException {
        service.shutdown();
        if (service.awaitTermination(10, TimeUnit.SECONDS)) {
            Logger.info(this, "#shutdown(): succeeded");
        } else {
            Logger.warn(this, "#shutdown(): failed");
            service.shutdownNow();
            if (service.awaitTermination(10, TimeUnit.SECONDS)) {
                Logger.info(this, "#shutdown(): shutdownNow() succeeded");
            } else {
                Logger.error(this, "#shutdown(): failed to stop threads");
            }
        }
    }

    /**
     * Dispatcher of FTPThread.
     * @since 0.0.1
     */
    private static final class FtpThreadRunnable implements Runnable {

        /**
         * The thread to run.
         */
        private final transient FtpThread thread;

        /**
         * Constructor.
         * @param thrd The FTPThread
         */
        FtpThreadRunnable(final FtpThread thrd) {
            this.thread = thrd;
        }

        @Override
        public void run() {
            this.thread.dispatch();
        }
    }
}
