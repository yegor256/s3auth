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
import javax.net.ssl.SSLServerSocketFactory;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPReply;

/**
 * Convenient base superclass that creates a request dispatcher object for port
 * listener implementations such as {@link HttpFacade} or {@link FtpFacade}.
 *
 * @author Simon Njenga (simtuje@gmail.com)
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 */
@ToString
@EqualsAndHashCode(
    of = { "frontend", "backend", "sockets", "server", "secured" }
)
@SuppressWarnings({ "PMD.DoNotUseThreads", "PMD.TooManyMethods" })
@Loggable(Loggable.DEBUG)
class BaseFacade implements Closeable {

    /**
     * How many threads to use.
     */
    private static final int THREADS = Tv.HUNDRED;

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

    @Override
    public void close() throws IOException {
        try {
            this.shutdown(this.frontend);
            this.shutdown(this.backend);
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException(ex);
        } finally {
            IOUtils.closeQuietly(this.server);
            IOUtils.closeQuietly(this.secured);
        }
    }

    /**
     * Start listening to the ports.
     *
     * The default implementation is <em>empty</em>. This should be overridden
     * in the subclasses as necessary.
     */
    protected void listen() {
        // nothing to do, intentionally empty
    }

    /**
     * Report overflow problem to the socket and close it.
     *
     * The default implementation is <em>empty</em>. This should be overridden
     * in the subclasses as necessary.
     *
     * @param socket The socket to report to
     * @throws IOException If something wrong happens inside
     */
    protected void overflow(final Socket socket) throws IOException {
        // nothing to do, intentionally empty
    }

    /**
     * Dispatcher for either HttpThread or FTPThread.
     * @param thread The dispatcher for processing a thread
     * @param bckend The executor service
     */
    private void threadRunnableDispatcher(final Dispatchable thread,
        final ScheduledExecutorService bckend) {
        final Runnable runnable = new VerboseRunnable(
            new BaseFacade.ThreadRunnableDispatcher(thread), true, true
        );
        for (int idx = 0; idx < BaseFacade.THREADS; ++idx) {
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
     */
    private void listen(final ScheduledExecutorService frntend,
        final ServerSocket svr) {
        frntend.scheduleWithFixedDelay(
            new VerboseRunnable(
                new Runnable() {
                    @Override
                    public void run() {
                        BaseFacade.this.process(svr);
                    }
                }
            ),
            0L, 1L, TimeUnit.NANOSECONDS
        );
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
            BaseFacade.THREADS
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
     */
    private void process(final ServerSocket svr) {
        final Socket socket;
        try {
            socket = svr.accept();
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            if (!this.sockets.offer(socket, Tv.TEN, TimeUnit.SECONDS)) {
                try {
                    this.overflow(socket);
                    Logger.warn(this, "too many open connections");
                } catch (final IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(ex);
        } finally {
            IOUtils.closeQuietly(svr);
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
    * Dispatcher for either {@link HttpThread} or {@link FtpThread}.
    */
    private static final class ThreadRunnableDispatcher implements Runnable {

       /**
        * The thread to run.
        */
        private final transient Dispatchable thread;

       /**
        * Package-private constructor.
        * @param thrd The RequestDispatcher
        */
        ThreadRunnableDispatcher(final Dispatchable thrd) {
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

    /**
     * HTTP facade (port listener).
     *
     * <p>The class is instantiated in {@link Main}, once per application run.
     *
     * <p>The class is immutable and thread-safe.
     *
     * @author Simon Njenga (simtuje@gmail.com)
     * @author Yegor Bugayenko (yegor@tpc2.com)
     * @version $Id$
     * @since 0.1
     * @see Main
     */
    @ToString
    @SuppressWarnings("PMD.DoNotUseThreads")
    @Loggable(Loggable.DEBUG)
    protected static final class HttpFacade extends BaseFacade {

       /**
        * Base facade encapsulated.
        */
        private final transient BaseFacade facade;

       /**
        * Package-private constructor.
        * @param hosts Hosts
        * @param port Port number
        * @param sslport SSL port number.
        * @throws IOException If can't initialize
        */
        HttpFacade(@NotNull final Hosts hosts, final int port,
            final int sslport) throws IOException {
            super(
                2, "front", BaseFacade.THREADS, "back",
                    new ServerSocket(port),
                        SSLServerSocketFactory.getDefault()
                            .createServerSocket(sslport)
            );
            this.facade = this;
            final HttpThread thread = new HttpThread(
                this.facade.sockets, hosts
            );
            this.facade.threadRunnableDispatcher(thread, this.facade.backend);
        }

        @Override
        public void listen() {
            this.facade.listen(this.facade.frontend, this.facade.server);
            this.facade.listen(this.facade.frontend, this.facade.secured);
        }

        @Override
        public void overflow(final Socket socket)
            throws IOException {
            new HttpResponse()
                .withStatus(HttpURLConnection.HTTP_GATEWAY_TIMEOUT)
                .withBody(this.facade.underHighLoad("site")).send(socket);
        }
    }

    /**
     * FTP facade (port listener).
     *
     * <p>The class is instantiated in {@link Main}, once per application run.
     *
     * <p>The class is immutable and thread-safe.
     *
     * @author Simon Njenga (simtuje@gmail.com)
     * @author Felipe Pina (felipe.pina@gmail.com)
     * @version $Id$
     * @since 0.1
     * @see Main
     * @todo #213:30min Implement TLS secure port listening in a manner analogous
     * to {@link HttpFacade}.
     */
    @ToString
    @SuppressWarnings("PMD.DoNotUseThreads")
    @Loggable(Loggable.DEBUG)
    protected static final class FtpFacade extends BaseFacade {

       /**
        * Base facade encapsulated.
        */
        private final transient BaseFacade facade;

       /**
        * Package-private constructor.
        * @param hosts Hosts
        * @param port Port number
        * @throws IOException If can't initialize
        */
        FtpFacade(@NotNull final Hosts hosts, final int port)
            throws IOException {
            super(
                2, "FTP-front", BaseFacade.THREADS, "FTP-back",
                    new ServerSocket(port), null
            );
            this.facade = this;
            final FtpThread thread = new FtpThread(this.facade.sockets, hosts);
            this.facade.threadRunnableDispatcher(thread, this.facade.backend);
        }

        @Override
        public void listen() {
            this.facade.listen(this.facade.frontend, this.facade.server);
        }

        @Override
        public void overflow(final Socket socket) throws IOException {
            new FtpResponse()
                .withCode(FTPReply.SERVICE_NOT_AVAILABLE)
                .withText(this.facade.underHighLoad("service")).send(socket);
        }
    }
}
