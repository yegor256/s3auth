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
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Base superclass that creates a request dispatcher object for port listener
 * implementations such as {@link HttpFacade} or {@link FtpFacade}.
 *
 * <p>Passing in {@code true} for the "ishttp" flag in the method
 * {@link #executeDispatch(boolean, Hosts)} allows this class to create
 * {@link HttpThread} request dispatcher object. Else, it creates
 * {@link FtpThread} request dispatcher object. This request dispatcher
 * object created is later used by the respective facade subclass.
 *
 * <p>Subsequent method invocations where the "ishttp" flag is used, e.g
 *
 * {@link #listen(boolean)},
 * {@link #listen(ScheduledExecutorService, ServerSocket, boolean)},
 * {@link #process(ServerSocket, boolean)} and
 * {@link #overflow(Socket, boolean)}.
 *
 * allows control flow to happen in favour of the respective facade subclass.
 *
 * @author Simon Njenga (simtuje@gmail.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@ToString
@EqualsAndHashCode(of = { "sockets", "server", "secured" })
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
@Loggable(Loggable.DEBUG)
class BaseFacade implements Closeable {

    /**
     * How many threads to use.
     */
    protected static final int THREADS = Tv.HUNDRED;

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
    private final transient BlockingQueue<Socket> sockets =
        new SynchronousQueue<Socket>();

    /**
     * Server socket.
     */
    private final transient ServerSocket server;

    /**
     * Secured Server socket.
     */
    private final transient ServerSocket secured;

    /**
     * Protected constructor.
     * @param frontthreads The number of front threads
     * @param frontprefix The name of the front threads
     * @param backthreads The number of back threads
     * @param backprefix The name of the back threads
     * @param svr The server socket
     * @param secure The secure server socket
     * @checkstyle ParameterNumber (5 lines)
     */
    protected BaseFacade(final int frontthreads, final String frontprefix,
        final int backthreads, final String backprefix, final ServerSocket svr,
        final ServerSocket secure) {
        this.frontend = this.createThreadPool(frontthreads, frontprefix);
        this.backend = this.createThreadPool(backthreads, backprefix);
        this.server = svr;
        this.secured = secure;
    }

    /**
     * {@inheritDoc}
     */
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
        this.secured.close();
    }

    /**
     * Execute this dispatch.
     * @param ishttp Is it for the HTTP facade class?
     * @param hosts The Hosts
     */
    protected void executeDispatch(final boolean ishttp, final Hosts hosts) {
        final RequestDispatcher thread;
        if (ishttp) {
            thread = new HttpThread(this.sockets, hosts);
        } else {
            thread = new FtpThread(this.sockets, hosts);
        }
        this.threadRunnableDispatcher(thread, this.backend);
    }

    /**
     * Start listening to the ports.
     * @param ishttp Is it for the HTTP facade class?
     */
    protected void listen(final boolean ishttp) {
        if (ishttp) {
            this.listen(this.frontend, this.server, true);
            this.listen(this.frontend, this.secured, true);
        } else {
            this.listen(this.frontend, this.server, false);
        }
    }

    /**
     * Start listening to the ports.
     *
     * The default implementation is <em>empty</em>. This can be overridden
     * by subclasses as necessary.
     */
    protected void listen() {
        // no-op, intentionally empty
    }

    /**
     * Dispatcher for either HttpThread or FTPThread.
     * @param thread The dispatcher for processing a thread
     * @param bckend The executor service
     */
    private void threadRunnableDispatcher(final RequestDispatcher thread,
        final ScheduledExecutorService bckend) {
        final Runnable runnable = new VerboseRunnable(
            new BaseFacade.ThreadRunnableDispatcher(thread), true, true
        );
        for (int idx = 0; idx < THREADS; ++idx) {
            bckend.scheduleWithFixedDelay(
                runnable,
                0L, 1L, TimeUnit.NANOSECONDS
            );
        }
    }

    /**
     * Start listening to the ports.
     * @param frntend The executor service
     * @param svr The server socket
     * @param ishttp Is it for the HTTP facade class?
     */
    private void listen(final ScheduledExecutorService frntend,
        final ServerSocket svr, final boolean ishttp) {
        frntend.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        BaseFacade.this.process(svr, ishttp);
                    }
                }
            ),
            0L, 1L, TimeUnit.NANOSECONDS
        );
    }

    /**
     * Creates a thread pool that can schedule commands to run after a
     * given delay, or to execute periodically.
     * @param threads The number of threads to keep in the pool
     * @param prefix The name of the factory to use wit the executor.
     * @return A newly created scheduled thread pool
     */
    private ScheduledExecutorService createThreadPool(final int threads,
        final String prefix) {
        return Executors.newScheduledThreadPool(
            threads,
            new VerboseThreads(prefix)
        );
    }

    /**
     * Process one server socket.
     * @param svr The server socket
     * @param ishttp Is it for the HTTP facade class?
     */
    private void process(final ServerSocket svr, final boolean ishttp) {
        final Socket socket;
        try {
            socket = svr.accept();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            if (!this.sockets.offer(socket, Tv.TEN, TimeUnit.SECONDS)) {
                try {
                    this.overflow(socket, ishttp);
                    Logger.warn(this, "too many open connections");
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Report overflow problem to the socket and close it.
     * @param socket The socket to report to
     * @param ishttp Is it for the HTTP facade class?
     * @throws IOException If something wrong happens inside
     */
    private void overflow(final Socket socket, final boolean ishttp)
        throws IOException {
        if (ishttp) {
            new HttpResponse()
                .withStatus(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .withBody(this.underHighLoad("site")).send(socket);
        } else {
            new FtpResponse()
                .withCode(FTPReply.SERVICE_NOT_AVAILABLE)
                .withText(this.underHighLoad("service")).send(socket);
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
     * The service/site is under high load.
     * @param str The service/site
     * @return The message
     */
    private String underHighLoad(final String str) {
        return String.format(
            // @checkstyle LineLength (1 line)
            StringUtils.join("We're sorry, the ", str, " is under high load at the moment (%d open connections), please try again in a few minutes"),
            THREADS
        );
    }

    /**
    * Dispatcher for either HttpThread or FTPThread.
    */
    private static final class ThreadRunnableDispatcher implements Runnable {

        /**
        * The thread to run.
        */
        private final transient RequestDispatcher thread;

        /**
        * Constructor.
        * @param thrd The RequestDispatcher
        */
        ThreadRunnableDispatcher(final RequestDispatcher thrd) {
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
