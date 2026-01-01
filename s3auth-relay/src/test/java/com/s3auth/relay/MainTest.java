/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.log.VerboseRunnable;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Main}.
 *
 * @since 0.0.1
 */
final class MainTest {

    /**
     * Main can start and listen on port.
     * @throws Exception If there is some problem inside
     * @todo #33 Test doesn't work since AWS Dynamo config is not available
     *  in runtime. We should find a way to mock it properly.
     */
    @Test
    @Disabled
    @SuppressWarnings("PMD.DoNotUseThreads")
    void startsAndListensOnPort() throws Exception {
        final int port = PortMocker.reserve();
        final CountDownLatch done = new CountDownLatch(1);
        final Thread thread = new Thread(
            new VerboseRunnable(
                (Callable<Void>) () -> {
                    try {
                        Main.main(new String[]{Integer.toString(port)});
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
        TimeUnit.SECONDS.sleep(1);
        final URI uri = new URI(
            String.format("http://localhost:%d/version", port)
        );
        MatcherAssert.assertThat(
            uri.toURL().getContent(),
            Matchers.notNullValue()
        );
        thread.interrupt();
        MatcherAssert.assertThat(
            done.await(1, TimeUnit.SECONDS),
            Matchers.is(true)
        );
    }

}
