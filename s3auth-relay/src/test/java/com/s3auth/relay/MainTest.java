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
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
}
