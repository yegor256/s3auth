/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.log.VerboseRunnable;
import com.s3auth.hosts.Resource;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link HttpResponse}.
 * @since 0.0.1
 */
@SuppressWarnings("PMD.CyclomaticComplexity")
final class HttpResponseTest {

    /**
     * HttpResponse can send correct HTTP response.
     * @throws Exception If there is some problem inside
     */
    @Test
    void sendsDataToSocket() throws Exception {
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
    void sendsDataFromSlowResource() throws Exception {
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
                    new OutputStreamWriter(stream, StandardCharsets.UTF_8)
                );
                writer.print(content);
                writer.flush();
                return content.getBytes().length;
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
        final StringBuffer received = new StringBuffer(100);
        new Thread(
            new VerboseRunnable(
                (Callable<Void>) () -> {
                    final Socket reading = server.accept();
                    received.append(
                        IOUtils.toString(
                            reading.getInputStream(), StandardCharsets.UTF_8
                        )
                    );
                    reading.close();
                    done.countDown();
                    return null;
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
