/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.VerboseWire;
import com.jcabi.matchers.XhtmlMatchers;
import com.s3auth.hosts.HostsMocker;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.takes.Take;
import org.takes.http.FtRemote;
import org.takes.rq.RqFake;
import org.takes.rq.RqMethod;
import org.takes.rq.RqWithHeader;
import org.takes.rs.RsPrint;

/**
 * Test case for {@link TkApp}.
 * @since 0.2
 */
final class TkAppTest {

    /**
     * App can render front page.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersHomePage() throws Exception {
        final Take take = new TkApp(
            new HostsMocker().mock()
        );
        MatcherAssert.assertThat(
            XhtmlMatchers.xhtml(
                new RsPrint(
                    take.act(
                        new RqWithHeader(
                            new RqFake(RqMethod.GET, "/"),
                            "Accept", "application/xml"
                        )
                    )
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
    void rendersManyUrls() throws Exception {
        final Take take = new TkApp(
            new HostsMocker().mock()
        );
        final String[] paths = {
            "/robots.txt", "/xsl/index.xsl", "/css/layout.css",
            "/version", "/license", "/images/logo.svg",
            "/images/favicon.ico", "/images/github-small.png",
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
    void rendersHomePageViaHttp() throws Exception {
        final Take take = new TkApp(
            new HostsMocker().mock()
        );
        new FtRemote(take).exec(
            home -> {
                new JdkRequest(home)
                    .header("Accept", "text/html")
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(Matchers.containsString("<!DOCTYPE html"));
                new JdkRequest(home)
                    .through(VerboseWire.class)
                    .header("Accept", "application/xml")
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .as(XmlResponse.class)
                    .assertXPath("/page/version");
            }
        );
    }

}
