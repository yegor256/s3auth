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
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import javax.net.ssl.SSLServerSocketFactory;
import javax.validation.constraints.NotNull;

/**
 * HTTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 * All protocol-neutral machinery (executor services, socket queue, accept
 * loop, graceful shutdown) lives in {@link Facade}; this class only
 * provides HTTP-specific behavior.</p>
 *
 * <p>The class is mutable and thread-safe.</p>
 *
 * @see Main
 * @see Facade
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class HttpFacade implements Closeable {

    /**
     * The protocol-neutral facade doing all the work.
     */
    private final transient Facade facade;

    /**
     * Private ctor.
     *
     * @param fcd The protocol-neutral facade
     */
    private HttpFacade(final Facade fcd) {
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
     * @param sslport SSL port number
     * @return Opened facade
     * @throws IOException If can't initialize
     */
    static HttpFacade open(@NotNull final Hosts hosts, final int port,
        final int sslport) throws IOException {
        final BlockingQueue<Socket> sockets = new SynchronousQueue<>();
        final Facade facade = new Facade(
            Executors.newScheduledThreadPool(2, new VerboseThreads("front")),
            Executors.newScheduledThreadPool(
                Facade.THREADS, new VerboseThreads("back")
            ),
            sockets,
            Arrays.asList(
                new ServerSocket(port),
                SSLServerSocketFactory.getDefault().createServerSocket(sslport)
            ),
            new HttpThreadRunnable(new HttpThread(sockets, hosts)),
            HttpFacade::overflow,
            100L,
            1_000L
        );
        facade.start();
        return new HttpFacade(facade);
    }

    private static void overflow(final Socket socket) {
        try {
            new HttpResponse()
                .withStatus(HttpURLConnection.HTTP_GATEWAY_TIMEOUT).withBody(
                    String.format(
                        "We're sorry, the site is under high load at the moment (%d open connections), please try again in a few minutes",
                        Facade.THREADS
                    )
                )
                .send(socket);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
