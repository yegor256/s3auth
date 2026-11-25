/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.log.VerboseRunnable;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.ResourceMocker;
import com.s3auth.hosts.Version;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Main}.
 *
 * @since 0.0.1
 */
final class MainTest {

    /**
     * Main can start and respond on port.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void startsAndRespondsOnPort() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doReturn(
            new ResourceMocker().init()
                .withHeaders("Content-Type: text/plain")
                .mock()
        ).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        final int port = PortMocker.reserve();
        final Thread thread = new Thread(
            new VerboseRunnable(
                (Callable<Void>) () -> {
                    try {
                        Main.main(
                            new String[] {
                                "-p", Integer.toString(port),
                                "-s", Integer.toString(PortMocker.reserve()),
                            },
                            hosts
                        );
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                    return null;
                },
                false
            )
        );
        thread.start();
        TimeUnit.SECONDS.sleep(1L);
        MatcherAssert.assertThat(
            new URI(
                String.format("http://localhost:%d/version", port)
            ).toURL().getContent(),
            Matchers.notNullValue()
        );
        thread.interrupt();
    }

    /**
     * Main can start the FTP facade when the FTP port is configured.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void startsFtpFacadeWhenConfigured() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final int http = PortMocker.reserve();
        final int secured = PortMocker.reserve();
        final int ftp = PortMocker.reserve();
        final Thread thread = new Thread(
            new VerboseRunnable(
                (Callable<Void>) () -> {
                    try {
                        Main.main(
                            new String[] {
                                "-p", Integer.toString(http),
                                "-s", Integer.toString(secured),
                                "-f", Integer.toString(ftp),
                            },
                            hosts
                        );
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                    return null;
                },
                false
            )
        );
        thread.start();
        TimeUnit.SECONDS.sleep(1L);
        try (Socket socket = new Socket("localhost", ftp)) {
            MatcherAssert.assertThat(
                socket.isConnected(),
                Matchers.is(true)
            );
        }
        thread.interrupt();
    }

    /**
     * Main stops listening when interrupted.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void stopsListeningWhenInterrupted() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final int port = PortMocker.reserve();
        final CountDownLatch done = new CountDownLatch(1);
        final Thread thread = new Thread(
            new VerboseRunnable(
                (Callable<Void>) () -> {
                    try {
                        Main.main(
                            new String[] {
                                "-p", Integer.toString(port),
                                "-s", Integer.toString(PortMocker.reserve()),
                                "-d",
                            },
                            hosts
                        );
                    } catch (final InterruptedException ex) {
                        done.countDown();
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(ex);
                    }
                    return null;
                },
                false
            )
        );
        thread.start();
        TimeUnit.SECONDS.sleep(1L);
        thread.interrupt();
        MatcherAssert.assertThat(
            done.await(1L, TimeUnit.SECONDS),
            Matchers.is(true)
        );
    }

    /**
     * Main rolls back HTTP startup when the FTP port is occupied.
     *
     * <p>When the FTP port cannot be bound, {@link Main} must close the
     * already-started HTTP facade, releasing both HTTP ports and all its
     * worker threads, instead of leaving a half-started process behind.</p>
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void rollsBackHttpWhenFtpPortIsOccupied() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        try (ServerSocket occupied = new ServerSocket(0)) {
            final int ftp = occupied.getLocalPort();
            final int http = PortMocker.reserve();
            final int secured = PortMocker.reserve();
            final Collection<String> before = MainTest.workers();
            final AtomicReference<Throwable> failure = new AtomicReference<>();
            final Thread thread = new Thread(
                new VerboseRunnable(
                    (Callable<Void>) () -> {
                        Main.main(
                            new String[] {
                                "-p", Integer.toString(http),
                                "-s", Integer.toString(secured),
                                "-f", Integer.toString(ftp),
                            },
                            hosts
                        );
                        return null;
                    },
                    false
                )
            );
            thread.setUncaughtExceptionHandler(
                (thrd, throwable) -> failure.set(throwable)
            );
            thread.start();
            thread.join(TimeUnit.SECONDS.toMillis(30L));
            final Collection<String> problems = new ArrayList<>(0);
            if (failure.get() == null) {
                problems.add(
                    String.format(
                        "main() did not fail though FTP port %d is occupied",
                        ftp
                    )
                );
            }
            int after = MainTest.workers().size();
            int attempt = 0;
            while (after != before.size() && attempt < 50) {
                TimeUnit.MILLISECONDS.sleep(100L);
                after = MainTest.workers().size();
                ++attempt;
            }
            if (after != before.size()) {
                problems.add(
                    String.format(
                        "worker threads not released: %d alive, %d expected",
                        after, before.size()
                    )
                );
            }
            if (!MainTest.rebindable(http)) {
                problems.add(
                    String.format("HTTP port %d is not released", http)
                );
            }
            if (!MainTest.rebindable(secured)) {
                problems.add(
                    String.format(
                        "secured HTTP port %d is not released", secured
                    )
                );
            }
            MatcherAssert.assertThat(
                String.join(String.format("%n"), problems),
                problems,
                Matchers.empty()
            );
        }
    }

    private static Collection<String> workers() {
        final Collection<String> names = new ArrayList<>(0);
        for (final Thread thread : Thread.getAllStackTraces().keySet()) {
            final String name = thread.getName();
            if (name.startsWith("front") || name.startsWith("back")) {
                names.add(name);
            }
        }
        return names;
    }

    private static boolean rebindable(final int port) throws IOException {
        boolean free = false;
        boolean interrupted = false;
        int attempt = 0;
        while (!free && !interrupted && attempt < 50) {
            try (ServerSocket probe = new ServerSocket(port)) {
                if (probe.getLocalPort() == port) {
                    free = true;
                }
            } catch (final IOException ex) {
                try {
                    TimeUnit.MILLISECONDS.sleep(100L);
                } catch (final InterruptedException stop) {
                    Thread.currentThread().interrupt();
                    interrupted = true;
                }
            }
            ++attempt;
        }
        return free;
    }
}
