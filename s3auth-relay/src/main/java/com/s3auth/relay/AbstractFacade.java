/**
 * Copyright (c) 2012-2015, s3auth.com
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
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Abstract facade (port listener).
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @author Simon Njenga (simtuje@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode(of = { "sockets", "server", "secured" })
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
@Loggable(Loggable.DEBUG)
abstract class AbstractFacade implements Closeable {

    /**
     * How many threads to use.
     */
    protected static final int THREADS = Tv.HUNDRED;

    /**
     * Executor service, with socket openers.
     */
    private transient ScheduledExecutorService frontend;

    /**
     * Executor service, with consuming threads.
     */
    private transient ScheduledExecutorService backend;

    /**
     * Blocking queue of ready-to-be-processed sockets.
     */
    private final transient BlockingQueue<Socket> sockets =
        new SynchronousQueue<Socket>();

    /**
     * Server socket.
     */
    private transient ServerSocket server;

    /**
     * Secured Server socket.
     */
    private transient ServerSocket secured;

    @Override
    public void close() throws IOException {
        try {
            this.shutdown(this.getFrontend());
            this.shutdown(this.getBackend());
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        }
        this.getServer().close();
        this.getSecured().close();
    }

    /**
     * Get frontend.
     * @return The frontend
     */
    protected final ScheduledExecutorService getFrontend() {
        return this.frontend;
    }

    /**
     * Set frontend.
     * @param frntend The frontend to set
     */
    protected final void setFrontend(final ScheduledExecutorService frntend) {
        this.frontend = frntend;
    }

    /**
     * Get backend.
     * @return The backend
     */
    protected final ScheduledExecutorService getBackend() {
        return this.backend;
    }

    /**
     * Set backend.
     * @param bckend The backend to set
     */
    protected final void setBackend(final ScheduledExecutorService bckend) {
        this.backend = bckend;
    }

    /**
     * Get server socket.
     * @return The server
     */
    protected final ServerSocket getServer() {
        return this.server;
    }

    /**
     * Set server socket.
     * @param svr The server socket to set
     */
    protected final void setServer(final ServerSocket svr) {
        this.server = svr;
    }

    /**
     * Get secured server socket.
     * @return The secured server
     */
    protected final ServerSocket getSecured() {
        return this.secured;
    }

    /**
     * Set secured server socket.
     * @param securd The secured server socket to set
    */
    protected final void setSecured(final ServerSocket securd) {
        this.secured = securd;
    }

    /**
     * Get the sockets.
     * @return The sockets
     */
    protected final BlockingQueue<Socket> getSockets() {
        return this.sockets;
    }

    /**
     * Start listening to the ports.
     * @param frntend The executor service
     * @param svr The server socket
     */
    protected void listen(final ScheduledExecutorService frntend,
        final ServerSocket svr) {
        frntend.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        AbstractFacade.this.process(svr);
                    }
                }
            ),
            0L, 1L, TimeUnit.NANOSECONDS
        );
    }

    /**
     * Dispatcher for either HttpThread or FTPThread.
     * @param thread The dispatcher for processing a thread
     * @param bckend The executor service
     */
    protected void threadRunnableDispatcher(final RequestDispatcher thread,
        final ScheduledExecutorService bckend) {
        final Runnable runnable = new VerboseRunnable(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        thread.dispatch();
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        Logger.warn(this, "%s", ex);
                    }
                }
            }
            , true, true
        );
        for (int idx = 0; idx < THREADS; ++idx) {
            bckend.scheduleWithFixedDelay(
                runnable,
                0L, 1L, TimeUnit.NANOSECONDS
            );
        }
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param threads The number of threads to keep in the pool
     * @param prefix The name of the factory to use wit the executor.
     * @return A newly created scheduled thread pool
     */
    protected ScheduledExecutorService createThreadPool(final int threads,
        final String prefix) {
        return Executors.newScheduledThreadPool(
            threads,
            new VerboseThreads(prefix)
        );
    }

    /**
     * Report overflow problem to the socket and close it.
     * @param socket The socket to report to
     */
    abstract void overflow(final Socket socket);

    /**
     * Shutdown a service.
     * @param service The service to shut down
     * @throws InterruptedException If fails to shutdown
     */
    private void shutdown(final ExecutorService service)
        throws InterruptedException {
        service.shutdown();
        if (service.awaitTermination(Tv.TEN, TimeUnit.SECONDS)) {
            Logger.info(this, "#shutdown(): succeeded");
        } else {
            Logger.warn(this, "#shutdown(): failed");
            service.shutdownNow();
            if (service.awaitTermination(Tv.TEN, TimeUnit.SECONDS)) {
                Logger.info(this, "#shutdown(): shutdownNow() succeeded");
            } else {
                Logger.error(this, "#shutdown(): failed to stop threads");
            }
        }
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
            if (!this.getSockets().offer(socket, Tv.TEN, TimeUnit.SECONDS)) {
                this.overflow(socket);
                Logger.warn(this, "too many open connections");
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }
}
