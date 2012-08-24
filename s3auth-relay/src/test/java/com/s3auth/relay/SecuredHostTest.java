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

import com.s3auth.hosts.HostMocker;
import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SecuredHost}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class SecuredHostTest {

    /**
     * SecuredHost can request authorization.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void requestsAuthorization() throws Exception {
        final String[] requests = new String[] {
            "GET / HTTP/1.1\nHost: example.com\n\n",
            "GET / HTTP/1.1\n",
        };
        for (String http : requests) {
            try {
                new SecuredHost(
                    new HostMocker().mock(),
                    this.toRequest(http)
                ).fetch(URI.create("/"));
                Assert.fail("exception expected");
            } catch (HttpException ex) {
                MatcherAssert.assertThat(
                    this.toString(ex.response()),
                    Matchers.containsString(HttpHeaders.WWW_AUTHENTICATE)
                );
            }
        }
    }

    /**
     * SecuredHost can detect incorrect data.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void requestsAuthorizationWhenBrokenData() throws Exception {
        final String[] requests = new String[] {
            "GET / HTTP/1.1\nAuthorization: xxx\n\n",
            "GET / HTTP/1.1\nAuthorization: Basic a1b2c3==\n\n",
            "GET / HTTP/1.1\nAuthorization: Basic \n\n",
        };
        for (String http : requests) {
            try {
                new SecuredHost(
                    new HostMocker().mock(),
                    this.toRequest(http)
                ).fetch(URI.create("/test.html"));
                Assert.fail("exception expected, but didn't happen");
            } catch (HttpException ex) {
                MatcherAssert.assertThat(
                    this.toString(ex.response()),
                    Matchers.startsWith(
                        String.format(
                            "HTTP/1.1 %d ",
                            HttpURLConnection.HTTP_BAD_REQUEST
                        )
                    )
                );
            }
        }
    }

    /**
     * Convert string to request.
     * @param text The text
     * @return Requests
     * @throws Exception If there is some problem inside
     */
    private HttpRequest toRequest(final String text) throws Exception {
        final Socket socket = Mockito.mock(Socket.class);
        Mockito.doReturn(IOUtils.toInputStream(text))
            .when(socket).getInputStream();
        return new HttpRequest(socket);
    }

    /**
     * Convert response to string.
     * @param resp The response
     * @return Text form
     * @throws Exception If there is some problem inside
     */
    private String toString(final HttpResponse resp) throws Exception {
        final Socket socket = Mockito.mock(Socket.class);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Mockito.doReturn(stream).when(socket).getOutputStream();
        resp.send(socket);
        return new String(stream.toByteArray());
    }

}
