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

import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rexsl.page.CookieBuilder;
import com.rexsl.test.JsonDocument;
import com.rexsl.test.RestTester;
import com.s3auth.hosts.User;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.lang.CharEncoding;

/**
 * Google-related resources.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle MultipleStringLiterals (500 lines)
 */
@Path("/google")
public final class GoogleRs extends BaseRs {

    /**
     * Facebook callback.
     * @param code The code from Facebook
     * @return The JAX-RS response
     */
    @GET
    @Path("/callback")
    @Loggable(Loggable.DEBUG)
    public Response callback(@QueryParam("code") final String code) {
        if (code == null) {
            throw new IllegalArgumentException("'code' is mandatory");
        }
        final User user = this.fetch(this.token(code));
        return Response.status(Response.Status.SEE_OTHER)
            .location(this.uriInfo().getBaseUri())
            .cookie(
                new CookieBuilder(this.uriInfo().getBaseUri())
                    .name(BaseRs.COOKIE)
                    .value(new CryptedUser(user).toString())
                    .temporary()
                    .build()
            )
            .build();
    }

    /**
     * Retrieve facebook access token.
     * @param code Facebook "authorization code"
     * @return The token
     */
    private String token(final String code) {
        return RestTester
            .start(URI.create("https://accounts.google.com/o/oauth2/token"))
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .post(
                "getting access_token from Google",
                String.format(
                    // @checkstyle LineLength (1 line)
                    "client_id=%s&redirect_uri=%s&client_secret=%s&grant_type=authorization_code&code=%s",
                    GoogleRs.encode(Manifests.read("S3Auth-GoogleId")),
                    GoogleRs.encode(
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path(GoogleRs.class)
                            .path(GoogleRs.class, "callback")
                            .build()
                            .toString()
                    ),
                    GoogleRs.encode(Manifests.read("S3Auth-GoogleSecret")),
                    GoogleRs.encode(code)
                )
            )
            .assertStatus(HttpURLConnection.HTTP_OK)
            .json("access_token")
            .get(0);
    }

    /**
     * Get user name from Google, but the code provided.
     * @param token Facebook access token
     * @return The user found in FB
     */
    private User fetch(final String token) {
        final URI uri = UriBuilder
            .fromPath("https://www.googleapis.com/oauth2/v1/userinfo")
            .queryParam("alt", "json")
            .queryParam("access_token", "{token}")
            .build(token);
        final JsonDocument json = RestTester.start(uri).get("user info");
        // @checkstyle AnonInnerLength (50 lines)
        return new User() {
            @Override
            public URN identity() {
                return URN.create(
                    String.format("urn:google:%s", json.json("id").get(0))
                );
            }
            @Override
            public String name() {
                return json.json("name").get(0);
            }
            @Override
            public URI photo() {
                final List<String> pics = json.json("picture");
                URI photo;
                if (pics.isEmpty()) {
                    photo = URI.create("http://img.s3auth.com/unknown.png");
                } else {
                    photo = URI.create(pics.get(0));
                }
                return photo;
            }
        };
    }

    /**
     * URL encode given text.
     * @param text The text to encode
     * @return Encoded
     */
    private static String encode(final String text) {
        try {
            return URLEncoder.encode(text, CharEncoding.UTF_8);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
