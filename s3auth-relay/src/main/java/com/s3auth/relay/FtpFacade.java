/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
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
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.0.1
 * @see Main
 * @todo #213:30min Implement TLS secure port listening in a manner analogous to
 *  HttpFacade.
 */
@ToString
@EqualsAndHashCode(of = { "sockets", "server" })
@SuppressWarnings("PMD.DoNotUseThreads")
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
     * Public ctor.
     * @param hosts Hosts
     * @param port Port number
     * @throws IOException If can't initialize
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    FtpFacade(@NotNull final Hosts hosts, final int port)
        throws IOException {
        this.frontend = Executors.newScheduledThreadPool(
            2, new VerboseThreads("FTP-front")
        );
        this.backend = Executors.newScheduledThreadPool(
            FtpFacade.THREADS,
            new VerboseThreads("FTP-back")
        );
        this.sockets = new SynchronousQueue<>();
        this.server = new ServerSocket(port);
        final FtpThread thread = new FtpThread(this.sockets, hosts);
        final Runnable runnable = new VerboseRunnable(
            new FtpThreadRunnable(thread), true, true
        );
        for (int idx = 0; idx < FtpFacade.THREADS; ++idx) {
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
            new VerboseRunnable(() -> this.process(this.server)),
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
            .withCode(FTPReply.SERVICE_NOT_AVAILABLE)
            .withText(
                String.format(
                    // @checkstyle LineLength (1 line)
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
     *
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
