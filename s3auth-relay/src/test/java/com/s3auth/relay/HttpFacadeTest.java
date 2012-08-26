/**
 * Copyright (c) 2012, s3auth.com
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
import com.jcabi.log.VerboseThreads;
import com.rexsl.test.RestTester;
import com.s3auth.hosts.HostsMocker;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link HttpFacade}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class HttpFacadeTest {

    /**
     * Port to use for facade (defined in pom.xml).
     */
    private final transient int port =
        Integer.valueOf(System.getProperty("http.port"));

    /**
     * URI of facade home.
     */
    private final transient URI uri =
        URI.create(String.format("http://localhost:%d/", this.port));

    /**
     * HttpFacade can process parallel requests.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void handlesParallelHttpRequests() throws Exception {
        final HttpFacade facade =
            new HttpFacade(new HostsMocker().mock(), this.port);
        facade.listen();
        final int threads = 50;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(threads);
        final URI path = UriBuilder.fromUri(this.uri).path("/abc").build();
        final Callable<?> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                RestTester.start(path)
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        String.format(
                            "Basic %s",
                            Base64.encodeBase64String("a:b".getBytes())
                        )
                    )
                    .get("read sample page")
                    .assertStatus(HttpURLConnection.HTTP_OK);
                finished.countDown();
                return null;
            }
        };
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(new VerboseRunnable(task, true));
        }
        start.countDown();
        MatcherAssert.assertThat(
            finished.await(1, TimeUnit.MINUTES),
            Matchers.is(true)
        );
        svc.shutdown();
        facade.close();
    }

}
