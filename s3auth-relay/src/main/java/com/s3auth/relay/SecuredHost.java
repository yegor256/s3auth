/**
 * Copyright (c) 2012-2017, s3auth.com
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

import com.jcabi.aspects.Loggable;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.Stats;
import com.s3auth.hosts.Version;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;

/**
 * Single HTTP processing thread.
 *
 * <p>It's a wrapper around {@link Host}, that adds HTTP Basic Auth mechanism
 * to a normal HTTP request processing. The class is instantiated in
 * {@link HttpThread}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @see HttpThread
 */
@ToString(of = "host")
@EqualsAndHashCode(of = { "host", "request" })
@Loggable(Loggable.DEBUG)
final class SecuredHost implements Host {

    /**
     * Authorization header pattern.
     */
    private static final Pattern AUTH_PATTERN =
        Pattern.compile("Basic ([a-zA-Z0-9/+]+=*)");

    /**
     * Original host.
     */
    private final transient Host host;

    /**
     * Http request to process.
     */
    private final transient HttpRequest request;

    /**
     * Public ctor.
     * @param hst Original host
     * @param rqst The request
     */
    SecuredHost(@NotNull final Host hst,
        @NotNull final HttpRequest rqst) {
        this.host = hst;
        this.request = rqst;
    }

    @Override
    @Loggable(value = Loggable.DEBUG, ignore = IOException.class)
    public Resource fetch(@NotNull final URI uri, @NotNull final Range range,
        @NotNull final Version version)throws IOException {
        final Resource res;
        if (this.isHidden(uri)) {
            res = this.secured(uri, range, version);
        } else {
            res = this.host.fetch(uri, range, version);
        }
        return res;
    }

    @Override
    public boolean isHidden(@NotNull final URI uri) throws IOException {
        return this.host.isHidden(uri);
    }

    @Override
    public boolean authorized(@NotNull final String user,
        @NotNull final String password) throws IOException {
        return this.host.authorized(user, password);
    }

    @Override
    public void close() throws IOException {
        this.host.close();
    }

    @Override
    public String syslog() {
        return this.host.syslog();
    }

    @Override
    public Stats stats() {
        return this.host.stats();
    }

    /**
     * Fetch this URI in a secure way.
     * @param uri The URI to fetch
     * @param range The range
     * @param version The object version
     * @return Fetched resource
     * @throws IOException If some IO problem inside
     */
    private Resource secured(final URI uri, final Range range,
        final Version version) throws IOException {
        if (!this.request.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new HttpException(
                new HttpResponse()
                    .withStatus(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .withHeader(
                        HttpHeaders.WWW_AUTHENTICATE,
                        String.format(
                            "Basic realm=\"%s\"",
                            this.request.headers().get(HttpHeaders.HOST)
                                .iterator().next()
                        )
                    )
            );
        }
        final Matcher matcher = SecuredHost.AUTH_PATTERN.matcher(
            this.request.headers().get(HttpHeaders.AUTHORIZATION)
                .iterator().next()
        );
        if (!matcher.matches()) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                String.format(
                    "'%s' header is in wrong format",
                    HttpHeaders.AUTHORIZATION
                )
            );
        }
        final String[] parts = new String(
                Base64.decodeBase64(matcher.group(1)),
                StandardCharsets.UTF_8
            ).split(":", 2);
        if (parts.length != 2) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                "should be two parts in Basic auth header"
            );
        }
        if (!this.authorized(parts[0], parts[1])) {
            throw new HttpException(
                new HttpResponse()
                    .withStatus(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .withHeader(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Basic realm=\"try again\""
                    )
                    .withBody(this.host.toString())
            );
        }
        return this.host.fetch(uri, range, version);
    }

}
