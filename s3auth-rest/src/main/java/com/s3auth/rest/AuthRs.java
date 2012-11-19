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
package com.s3auth.rest;

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.restfb.DefaultFacebookClient;
import com.rexsl.page.CookieBuilder;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.test.RestTester;
import com.s3auth.hosts.User;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Authentication mechanism.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/a")
public final class AuthRs extends BaseRs {

    /**
     * Show entrance page.
     * @return The JAX-RS response
     */
    @GET
    @Path("/")
    public Response entrance() {
        return new PageBuilder()
            .stylesheet("/xsl/entrance.xsl")
            .build(CommonPage.class)
            .init(this)
            .link(
                new Link(
                    "facebook-auth",
                    UriBuilder
                        .fromUri("https://www.facebook.com/dialog/oauth")
                        .queryParam("client_id", "{id}")
                        .queryParam("redirect_uri", "{uri}")
                        .build(
                            Manifests.read("S3Auth-FbId"),
                            this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(FacebookRs.class, "callback")
                                .build()
                        )
                )
            )
            .link(
                new Link(
                    "google-auth",
                    UriBuilder
                        .fromUri("https://accounts.google.com/o/oauth2/auth")
                        .queryParam("client_id", "{id}")
                        .queryParam("redirect_uri", "{uri}")
                        .queryParam("scope", "https://www.googleapis.com/auth/userinfo.profile")
                        .build(
                            Manifests.read("S3Auth-GoogleId"),
                            this.uriInfo().getBaseUriBuilder()
                                .clone()
                                .path(GoogleRs.class, "callback")
                                .build()
                        )
                )
            )
            .render()
            .build();
    }

    /**
     * Log out.
     * @return The JAX-RS response
     */
    @GET
    @Path("/out")
    public Response logout() {
        return Response.status(Response.Status.SEE_OTHER)
            .location(this.uriInfo().getBaseUri())
            .cookie(
                new CookieBuilder(this.uriInfo().getBaseUri())
                    .name(BaseRs.COOKIE)
                    .build()
            )
            .build();
    }

}
