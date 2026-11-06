/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.VerboseThreads;
import com.s3auth.hosts.Hosts;
import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import javax.validation.constraints.NotNull;
import org.apache.commons.net.ftp.FTPReply;

/**
 * FTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 * All protocol-neutral machinery (executor services, socket queue, accept
 * loop, graceful shutdown) lives in {@link Facade}; this class only
 * provides FTP-specific behavior.</p>
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @see Main
 * @see Facade
 * @since 0.0.1
 * @todo #213:30min Implement TLS secure port listening in a manner analogous to
 *  HttpFacade.
 */
@Loggable(Loggable.DEBUG)
final class FtpFacade implements Closeable {

    /**
     * The protocol-neutral facade doing all the work.
     */
    private final transient Facade facade;

    /**
     * Private ctor.
     *
     * @param fcd The protocol-neutral facade
     */
    private FtpFacade(final Facade fcd) {
        this.facade = fcd;
    }

    @Override
    public void close() throws IOException {
        this.facade.close();
    }

    /**
     * Start listening to the ports.
     */
    void listen() {
        this.facade.listen();
    }

    /**
     * Open a facade and start its backend threads.
     *
     * @param hosts Hosts
     * @param port Port number
     * @return Opened facade
     * @throws IOException If can't initialize
     */
    static FtpFacade open(@NotNull final Hosts hosts, final int port)
        throws IOException {
        final BlockingQueue<Socket> sockets = new SynchronousQueue<>();
        final Facade facade = new Facade(
            Executors.newScheduledThreadPool(
                2, new VerboseThreads("FTP-front")
            ),
            Executors.newScheduledThreadPool(
                Facade.THREADS, new VerboseThreads("FTP-back")
            ),
            sockets,
            Collections.singletonList(new ServerSocket(port)),
            new FtpThreadRunnable(new FtpThread(sockets, hosts)),
            FtpFacade::overflow,
            10_000L,
            10_000L
        );
        facade.start();
        return new FtpFacade(facade);
    }

    private static void overflow(final Socket socket) {
        new FtpResponse()
            .withCode(FTPReply.SERVICE_NOT_AVAILABLE).withText(
                String.format(
                    "We're sorry, the service is under high load at the moment (%d open connections), please try again in a few minutes",
                    Facade.THREADS
                )
            )
            .send(socket);
    }
}
