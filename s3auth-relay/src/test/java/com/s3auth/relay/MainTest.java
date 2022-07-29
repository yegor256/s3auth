/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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

import com.jcabi.log.VerboseRunnable;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Main}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MainTest {

    /**
     * Main can start and listen on port.
     * @throws Exception If there is some problem inside
     * @todo #33 Test doesn't work since AWS Dynamo config is not available
     *  in runtime. We should find a way to mock it properly.
     */
    @Test
    @org.junit.Ignore
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void startsAndListensOnPort() throws Exception {
        final int port = PortMocker.reserve();
        final CountDownLatch done = new CountDownLatch(1);
        final Thread thread = new Thread(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            Main.main(new String[]{Integer.toString(port)});
                        } catch (final InterruptedException ex) {
                            done.countDown();
                            Thread.currentThread().interrupt();
                            throw new IllegalStateException(ex);
                        }
                        return null;
                    }
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
