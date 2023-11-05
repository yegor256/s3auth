/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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
import com.s3auth.hosts.Stats;
import com.s3auth.hosts.Version;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SecuredHost}.
 */
@SuppressWarnings({
    "PMD.AvoidInstantiatingObjectsInLoops",
    "PMD.TooManyMethods"
})
final class SecuredHostTest {

    /**
     * SecuredHost can request authorization.
     * @throws Exception If there is some problem inside
     */
    @Test
    void requestsAuthorization() throws Exception {
        final String[] hosts = {
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
                ).fetch(URI.create("/"), Range.ENTIRE, Version.LATEST);
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
    void requestsAuthorizationWhenBrokenData() throws Exception {
        final String[] requests = {
            "GET / HTTP/1.1\nAuthorization: xxx\n\n",
            "GET / HTTP/1.1\nAuthorization: Basic a1b2c3==\n\n",
            "GET / HTTP/1.1\nAuthorization: Basic \n\n",
        };
        for (final String http : requests) {
            try {
                new SecuredHost(
                    new HostMocker().mock(),
                    HttpRequestMocker.toRequest(http)
                ).fetch(URI.create("/test.html"), Range.ENTIRE, Version.LATEST);
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
    void reportsToStringWhenAuthorizationFails() throws Exception {
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
                    public Resource fetch(final URI uri, final Range range,
                        final Version version) {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public void close() {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public String syslog() {
                        throw new UnsupportedOperationException();
                    }
                    @Override
                    public Stats stats() {
                        throw new UnsupportedOperationException();
                    }
                },
                HttpRequestMocker.toRequest(
                    "GET / HTTP/1.1\nAuthorization: Basic dGVzdDp0ZXN0\n\n"
                )
            ).fetch(
                URI.create("/test-request.html"), Range.ENTIRE, Version.LATEST
            );
            Assert.fail("authorization failed expected, but didn't happen");
        } catch (final HttpException ex) {
            MatcherAssert.assertThat(
                HttpResponseMocker.toString(ex.response()),
                Matchers.containsString("hello")
            );
        }
    }

    /**
     * SecuredHost can use credentials containing special characters.
     * @throws Exception If something wrong occurs
     */
    @Test
    void recognizesCredentialsWithSpecialCharacters() throws Exception {
        final String user = "user";
        final String password = "password%oD\u20ac";
        final Host host = Mockito.mock(Host.class);
        final Resource res = Mockito.mock(Resource.class);
        Mockito.doReturn(res).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        Mockito.doReturn(true).when(host).authorized(user, password);
        Mockito.doReturn(true).when(host).isHidden(Mockito.any(URI.class));
        MatcherAssert.assertThat(
            new SecuredHost(
                host,
                HttpRequestMocker.toRequest(
                    String.format(
                        "GET / HTTP/1.1\nAuthorization: Basic %s\n\n",
                        Base64.encodeBase64String(
                            String.format("%s:%s", user, password)
                                .getBytes(CharEncoding.UTF_8)
                        )
                    )
                )
            ).fetch(
                URI.create("/test-special.html"), Range.ENTIRE, Version.LATEST
            ),
            Matchers.is(res)
        );
    }

    /**
     * SecuredHost can accept Base64 header.
     * @throws Exception If something wrong occurs
     */
    @Test
    void acceptsBaseEncoding() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doReturn(true).when(host).authorized(
            Mockito.anyString(), Mockito.anyString()
        );
        Mockito.doReturn(true).when(host).isHidden(Mockito.any(URI.class));
        MatcherAssert.assertThat(
            new SecuredHost(
                host,
                HttpRequestMocker.toRequest(
                    "GET / HTTP/1.1\nAuthorization: Basic YT+hYTp4\n\n"
                )
            ).fetch(new URI("#1"), Range.ENTIRE, Version.LATEST),
            Matchers.nullValue()
        );
    }
}
