/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.log.VerboseRunnable;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Protocol-neutral facade (port listener).
 *
 * <p>This class owns all the protocol-neutral machinery of a network
 * facade: the executor services, the queue of accepted sockets, the
 * accept loop and the graceful shutdown. What is protocol-specific
 * &mdash; the runnable consuming sockets from the queue and the response
 * sent back when the queue overflows &mdash; is provided by the
 * decorating facades, like {@link HttpFacade} and {@link FtpFacade},
 * which construct this class and delegate their protocol-neutral work
 * to it.</p>
 *
 * <p>The class is instantiated once per application run.</p>
 *
 * <p>The class is mutable and thread-safe.</p>
 *
 * @see HttpFacade
 * @see FtpFacade
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class Facade implements Closeable {

    /**
     * How many threads to use.
     */
    static final int THREADS = 100;

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
     * Server sockets to listen to.
     */
    private final transient Collection<ServerSocket> servers;

    /**
     * The runnable consuming sockets from the queue.
     */
    private final transient Runnable worker;

    /**
     * Sends a protocol-specific response to a socket we can't process.
     */
    private final transient Consumer<Socket> rejected;

    /**
     * How long to wait for threads to stop gracefully, in milliseconds.
     */
    private final transient long patience;

    /**
     * How long to wait for threads to stop forcibly, in milliseconds.
     */
    private final transient long force;

    /**
     * Ctor, threads started by {@link #start}.
     *
     * @param frnt Executor service, with socket openers
     * @param back Executor service, with consuming threads
     * @param skts Blocking queue of ready-to-be-processed sockets
     * @param srvs Server sockets to listen to
     * @param wrkr The runnable consuming sockets from the queue
     * @param rjct Sends a protocol-specific response to a rejected socket
     * @param pnc How long to wait for threads to stop gracefully, in millis
     * @param frc How long to wait for threads to stop forcibly, in millis
     */
    Facade(final ScheduledExecutorService frnt,
        final ScheduledExecutorService back,
        final BlockingQueue<Socket> skts, final Collection<ServerSocket> srvs,
        final Runnable wrkr, final Consumer<Socket> rjct,
        final long pnc, final long frc) {
        this.frontend = frnt;
        this.backend = back;
        this.sockets = skts;
        this.servers = srvs;
        this.worker = wrkr;
        this.rejected = rjct;
        this.patience = pnc;
        this.force = frc;
    }

    @Override
    @SuppressWarnings("PMD.CloseResource")
    public void close() throws IOException {
        try {
            this.shutdown(this.frontend);
            this.shutdown(this.backend);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        for (final ServerSocket svr : this.servers) {
            svr.close();
        }
    }

    /**
     * Start listening to the ports.
     */
    @SuppressWarnings("PMD.CloseResource")
    void listen() {
        for (final ServerSocket svr : this.servers) {
            Logger.debug(
                this, "#listen(): scheduled %s",
                this.frontend.scheduleWithFixedDelay(
                    new VerboseRunnable(() -> this.process(svr)),
                    0L, 1L, TimeUnit.NANOSECONDS
                )
            );
        }
    }

    /**
     * Start backend threads, consuming sockets from the queue.
     */
    void start() {
        final Runnable runnable = new VerboseRunnable(this.worker, true, true);
        for (int idx = 0; idx < Facade.THREADS; ++idx) {
            final ScheduledFuture<?> future = this.backend
                .scheduleWithFixedDelay(runnable, 0L, 1L, TimeUnit.NANOSECONDS);
            Logger.debug(this, "#start(): scheduled %s", future);
        }
    }

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
                this.rejected.accept(socket);
                Logger.warn(
                    this,
                    "too many open connections (%d), can't open any more",
                    Facade.THREADS
                );
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    private void shutdown(final ExecutorService service)
        throws InterruptedException {
        service.shutdown();
        if (service.awaitTermination(this.patience, TimeUnit.MILLISECONDS)) {
            Logger.info(this, "#shutdown(): succeeded");
        } else {
            Logger.warn(this, "#shutdown(): failed");
            service.shutdownNow();
            if (service.awaitTermination(this.force, TimeUnit.MILLISECONDS)) {
                Logger.info(this, "#shutdown(): shutdownNow() succeeded");
            } else {
                Logger.error(this, "#shutdown(): failed to stop threads");
            }
        }
    }
}
