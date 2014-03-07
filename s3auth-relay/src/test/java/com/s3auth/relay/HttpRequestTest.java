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

import com.s3auth.hosts.Range;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link HttpRequest}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (300 lines)
 * @checkstyle MagicNumberCheck (300 lines)
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public final class HttpRequestTest {

    /**
     * HttpRequest can parse HTTP response.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void parsesHttpRequest() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            "GET /test.html HTTP/1.1\nHost:local\nAccept:text/plain\n\nbody"
        );
        MatcherAssert.assertThat(
            request.requestUri().toString(),
            Matchers.equalTo("/test.html")
        );
        MatcherAssert.assertThat(
            request.headers().get("Host"),
            Matchers.hasItem("local")
        );
    }

    /**
     * HttpRequest can retrieve headers in a case-insensitive manner.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fetchesCaseInsensitiveHeaders() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            "GET /test.html HTTP/1.1\nHost:local\nAccept:text/plain\n\nbody"
        );
        MatcherAssert.assertThat(
            request.headers().get("Accept"),
            Matchers.hasItem("text/plain")
        );
        MatcherAssert.assertThat(
            request.headers().get("ACCEPT"),
            Matchers.hasItem("text/plain")
        );
        MatcherAssert.assertThat(
            request.headers().get("accept"),
            Matchers.hasItem("text/plain")
        );
        MatcherAssert.assertThat(
            request.headers().get("aCcEpT"),
            Matchers.hasItem("text/plain")
        );
    }

    /**
     * HttpRequest can retrieve a full range header value.
     * @throws Exception If a problem occurs
     */
    @Test
    public void canFetchFullByteRange() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            new StringBuilder("GET /test.html HTTP/1.1\n")
               .append("Host:local\n")
               .append("Accept:text/plain\n")
               .append("Range: bytes=100-200\n\nbody")
               .toString()
        );
        final Range range = request.range();
        MatcherAssert.assertThat(
            range.first(),
            Matchers.is(100L)
        );
        MatcherAssert.assertThat(
            range.last(),
            Matchers.is(200L)
        );
    }

    /**
     * HttpRequest can retrieve a range with only the first byte specified.
     * e.g. "byte=100-" for "From byte 100"
     * @throws Exception If a problem occurs
     */
    @Test
    public void canFetchRangeFromFirstByte() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            new StringBuilder("GET /test.html HTTP/1.1\n")
                .append("Host:local\n")
                .append("Accept:text/plain\n")
                .append("Range: bytes=100-\n\nbody")
                .toString()
        );
        final Range range = request.range();
        MatcherAssert.assertThat(
            range.first(),
            Matchers.is(100L)
        );
        MatcherAssert.assertThat(
            range.last(),
            Matchers.is(Long.MAX_VALUE)
        );
    }

    /**
     * HttpRequest can retrieve a range for final bytes.
     * e.g. "byte -100" for "last 100 bytes"
     * @throws Exception If a problem occurs
     */
    @Test
    public void canFetchRangeForLastBytes() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
           new StringBuilder("GET /test.html HTTP/1.1\n")
                .append("Host:local\n")
                .append("Accept:text/plain\n")
                .append("Range: bytes=-200\n\nbody")
                .toString()
        );
        final Range range = request.range();
        MatcherAssert.assertThat(
            range.first(),
            Matchers.is(Long.MIN_VALUE)
        );
        MatcherAssert.assertThat(
            range.last(),
            Matchers.is(200L)
        );
    }

}
