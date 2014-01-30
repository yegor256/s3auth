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

import com.s3auth.hosts.Host;
import com.s3auth.hosts.HostMocker;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

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
        final String[] hosts = new String[] {
            "example.com",
            "maven.s3auth.com",
        };
        for (final String name : hosts) {
            try {
                new SecuredHost(
                    new HostMocker().mock(),
                    HttpRequestMocker.toRequest(
                        String.format("GET / HTTP/1.1\nHost: %s\n\n", name)
                    )
                ).fetch(URI.create("/"), Range.ENTIRE);
                Assert.fail("exception expected");
            } catch (final HttpException ex) {
                MatcherAssert.assertThat(
                    HttpResponseMocker.toString(ex.response()),
                    Matchers.allOf(
                        Matchers.containsString(HttpHeaders.WWW_AUTHENTICATE),
                        Matchers.containsString(
                            String.format("Basic realm=\"%s\"", name)
                        )
                    )
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
        for (final String http : requests) {
            try {
                new SecuredHost(
                    new HostMocker().mock(),
                    HttpRequestMocker.toRequest(http)
                ).fetch(URI.create("/test.html"), Range.ENTIRE);
                Assert.fail("exception expected, but didn't happen");
            } catch (final HttpException ex) {
                MatcherAssert.assertThat(
                    HttpResponseMocker.toString(ex.response()),
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
     * SecuredHost can report {@code Host#toString()} when authorization fails.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void reportsToStringWhenAuthorizationFails() throws Exception {
        try {
            new SecuredHost(
                // @checkstyle AnonInnerLength (50 lines)
                new Host() {
                    @Override
                    public String toString() {
                        return "hello, world!";
                    }
                    @Override
                    public boolean isHidden(final URI uri) {
                        return true;
                    }
                    @Override
                    public boolean authorized(final String user,
                        final String pwd) {
                        return false;
                    }
                    @Override
                    public Resource fetch(final URI uri, final Range range) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public void close() {
                        throw new UnsupportedOperationException();
                    }
                },
                HttpRequestMocker.toRequest(
                    "GET / HTTP/1.1\nAuthorization: Basic dGVzdDp0ZXN0\n\n"
                )
            ).fetch(URI.create("/test-request.html"), Range.ENTIRE);
            Assert.fail("authorization failed expected, but didn't happen");
        } catch (final HttpException ex) {
            MatcherAssert.assertThat(
                HttpResponseMocker.toString(ex.response()),
                Matchers.containsString("hello")
            );
        }
    }

}
