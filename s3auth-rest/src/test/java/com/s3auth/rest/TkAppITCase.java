/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpHeaders;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Test;

/**
 * Integration case for {@link TkApp}.
 * @since 0.5
 */
final class TkAppITCase {

    /**
     * Home page of Tomcat.
     */
    private static final String HOME = System.getProperty("takes.home");

    /**
     * Before the entire test.
     */
    @BeforeClass
    public static void before() {
        Assume.assumeNotNull(TkAppITCase.HOME);
    }

    /**
     * IndexRs can render absent pages.
     * @throws Exception If some problem inside
     */
    @Test
    void renderAbsentPages() throws Exception {
        final String[] pages = {
            "/page-doesnt-exist",
            "/xsl/xsl-stylesheet-doesnt-exist.xsl",
            "/css/stylesheet-is-absent.css",
        };
        final Request request = new JdkRequest(TkAppITCase.HOME);
        for (final String page : pages) {
            request.uri().path(page).back()
                .method(Request.GET)
                .header("Accept", "text/html")
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
                .assertBody(Matchers.containsString("page not found"));
        }
    }

    /**
     * IndexRs can render valid pages.
     * @throws Exception If some problem inside
     */
    @Test
    void rendersValidPages() throws Exception {
        final String[] pages = {
            "/robots.txt",
            "/xsl/layout.xsl",
            "/xsl/login.xsl",
            "/css/layout.css",
        };
        final Request request = new JdkRequest(TkAppITCase.HOME);
        for (final String page : pages) {
            request.uri().path(page).back()
                .method(Request.GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
    }

    /**
     * IndexRs can show version.
     * @throws Exception If some problem inside
     */
    @Test
    void showsVersion() throws Exception {
        new JdkRequest(TkAppITCase.HOME)
            .uri().path("/").back()
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/page/version/name");
    }

}
