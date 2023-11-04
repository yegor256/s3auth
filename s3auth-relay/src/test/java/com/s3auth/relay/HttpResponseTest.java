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

import com.jcabi.aspects.Tv;
import com.jcabi.log.VerboseRunnable;
import com.s3auth.hosts.Resource;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link HttpResponse}.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
public final class HttpResponseTest {

    /**
     * HttpResponse can send correct HTTP response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void sendsDataToSocket() throws Exception {
        MatcherAssert.assertThat(
            HttpResponseMocker.toString(
                new HttpResponse()
                    .withStatus(HttpURLConnection.HTTP_NOT_FOUND)
                    .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                    .withBody("hi!")
            ),
            Matchers.allOf(
                Matchers.startsWith("HTTP/1.1 404"),
                Matchers.containsString("\n\nhi!")
            )
        );
    }

    /**
     * HttpResponse can process a slow resource (a few seconds waiting).
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.DoNotUseThreads")
    public void sendsDataFromSlowResource() throws Exception {
        final String content = "\u0433 some text";
        // @checkstyle AnonInnerLength (50 lines)
        final Resource res = new Resource() {
            @Override
            public long writeTo(final OutputStream stream) {
                try {
                    TimeUnit.SECONDS.sleep(2L);
                } catch (final InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
                final PrintWriter writer = new PrintWriter(
                    new OutputStreamWriter(stream, Charsets.UTF_8)
                );
                writer.print(content);
                writer.flush();
                return (long) content.getBytes().length;
            }
            @Override
            public int status() {
                return HttpURLConnection.HTTP_OK;
            }
            @Override
            public Collection<String> headers() {
                return Collections.emptyList();
            }
            @Override
            public String etag() {
                return "";
            }
            @Override
            public Date lastModified() {
                return new Date();
            }
            @Override
            public String contentType() {
                return "text/plain";
            }
            @Override
            // @checkstyle MethodBodyComments (2 lines)
            public void close() {
                // Nothing to do here.
            }
        };
        final HttpResponse response = new HttpResponse().withBody(res);
        final ServerSocket server = new ServerSocket(0);
        final CountDownLatch done = new CountDownLatch(1);
        final StringBuffer received = new StringBuffer(Tv.HUNDRED);
        new Thread(
            new VerboseRunnable(
                new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        final Socket reading = server.accept();
                        received.append(
                            IOUtils.toString(
                                reading.getInputStream(), Charsets.UTF_8
                            )
                        );
                        reading.close();
                        done.countDown();
                        return null;
                    }
                },
                true
            )
        ).start();
        final Socket writing = new Socket("localhost", server.getLocalPort());
        response.send(writing);
        writing.close();
        MatcherAssert.assertThat(
            done.await(1L, TimeUnit.SECONDS),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            received.toString(),
            Matchers.allOf(
                Matchers.startsWith("HTTP/1.1 200 OK"),
                Matchers.endsWith(content)
            )
        );
    }

}
