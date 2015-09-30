/**
 * Copyright (c) 2012-2015, s3auth.com
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
package com.s3auth.rest;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.VerboseWire;
import com.jcabi.matchers.XhtmlMatchers;
import com.s3auth.hosts.HostsMocker;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.takes.Take;
import org.takes.http.FtRemote;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkApp}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.2
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
public final class TkAppTest {

    /**
     * App can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersHomePage() throws Exception {
        final Take take = new TkApp(
            new HostsMocker().mock()
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(new RqFake(RqMethod.GET, "/"))
                ).printBody()
            ),
            XhtmlMatchers.hasXPaths(
                "/page/millis",
                "/page/links/link[@rel='add']",
                "/page/links/link[@rel='takes:github']"
            )
        );
    }

    /**
     * App can render all possible URLs.
     * @throws Exception If some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void rendersManyUrls() throws Exception {
        final Take take = new TkApp(
            new HostsMocker().mock()
        );
        final String[] paths = {
            "/robots.txt", "/xsl/index.xsl", "/css/layout.css",
            "/version", "/license",
        };
        for (final String path : paths) {
            MatcherAssert.assertThat(
                new RsPrint(
                    take.act(new RqFake(RqMethod.GET, path))
                ).print(),
                Matchers.startsWith("HTTP/1.1 200 ")
            );
        }
    }

    /**
     * App can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    public void rendersHomePageViaHttp() throws Exception {
        final Take take = new TkApp(
            new HostsMocker().mock()
        );
        new FtRemote(take).exec(
            new FtRemote.Script() {
                @Override
                public void exec(final URI home) throws IOException {
                    new JdkRequest(home)
                        .fetch()
                        .as(RestResponse.class)
                        .assertStatus(HttpURLConnection.HTTP_OK)
                        .as(XmlResponse.class)
                        .assertXPath("/xhtml:html");
                    new JdkRequest(home)
                        .through(VerboseWire.class)
                        .header("Accept", "application/xml")
                        .fetch()
                        .as(RestResponse.class)
                        .assertStatus(HttpURLConnection.HTTP_OK)
                        .as(XmlResponse.class)
                        .assertXPath("/page/version");
                }
            }
        );
    }

}
