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
import com.s3auth.hosts.Resource;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.CharEncoding;

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
final class SecuredHost implements Host {

    /**
     * Authorization header pattern.
     */
    private static final Pattern AUTH_PATTERN =
        Pattern.compile("Basic ([a-zA-Z0-9/]+=*)");

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
    public SecuredHost(@NotNull final Host hst,
        @NotNull final HttpRequest rqst) {
        this.host = hst;
        this.request = rqst;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource fetch(@NotNull final URI uri) throws IOException {
        if (!this.request.headers().containsKey(HttpHeaders.AUTHORIZATION)) {
            throw new HttpException(
                new HttpResponse()
                    .withStatus(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .withHeader(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Basic realm=\"s3auth\""
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
        String[] parts;
        try {
            parts = URLDecoder.decode(
                new String(
                    Base64.decodeBase64(matcher.group(1)),
                    CharEncoding.UTF_8
                ),
                CharEncoding.UTF_8
            ).split(":", 2);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        if (parts.length != 2) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                "should be two parts in Basic auth header"
            );
        }
        if (!this.host.authorized(parts[0], parts[1])) {
            throw new HttpException(
                new HttpResponse()
                    .withStatus(HttpURLConnection.HTTP_UNAUTHORIZED)
                    .withHeader(
                        HttpHeaders.WWW_AUTHENTICATE,
                        "Basic realm=\"try again\""
                    )
            );
        }
        return this.host.fetch(uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authorized(@NotNull final String user,
        @NotNull final String password)
        throws IOException {
        return this.host.authorized(user, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.host.close();
    }

}
