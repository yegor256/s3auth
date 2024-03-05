/*
 * Copyright (c) 2012-2024, Yegor Bugayenko
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link HttpRequest}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class HttpRequestTest {

    /**
     * HttpRequest can parse HTTP response.
     * @throws Exception If there is some problem inside
     */
    @Test
    void parsesHttpRequest() throws Exception {
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
    void fetchesCaseInsensitiveHeaders() throws Exception {
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
    void canFetchFullByteRange() throws Exception {
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
    void canFetchRangeFromFirstByte() throws Exception {
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
     * HttpRequest can retrieve query parameters.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesQueryParams() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            //@checkstyle LineLength (1 line)
            "GET /test.html?test=param&hello=world HTTP/1.1\nHost:local\nAccept:text/plain\n\nbody"
        );
        MatcherAssert.assertThat(
            request.parameters().get("test"),
            Matchers.hasItem("param")
        );
        MatcherAssert.assertThat(
            request.parameters().get("hello"),
            Matchers.hasItem("world")
        );
    }

    /**
     * HttpRequest can retrieve duplicate query parameters.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesDuplicateQueryParams() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            //@checkstyle LineLength (1 line)
            "GET /test.html?first=one&second=two&first=three HTTP/1.1\nHost:local\nAccept:text/plain\n\nbody"
        );
        MatcherAssert.assertThat(
            request.parameters().get("first"),
            Matchers.allOf(
                Matchers.iterableWithSize(2),
                Matchers.hasItems("one", "three")
            )
        );
        MatcherAssert.assertThat(
            request.parameters().get("second"),
            Matchers.hasItem("two")
        );
    }

    /**
     * HttpRequest can retrieve query parameters with no specified value.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesQueryParamsWithNoValue() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            //@checkstyle LineLength (1 line)
            "GET /test.html?blank&something=yes&nothing HTTP/1.1\nHost:local\nAccept:text/plain\n\nbody"
        );
        MatcherAssert.assertThat(
            request.parameters().get("blank"),
            Matchers.hasItem("")
        );
        MatcherAssert.assertThat(
            request.parameters().get("something"),
            Matchers.hasItem("yes")
        );
        MatcherAssert.assertThat(
            request.parameters().get("nothing"),
            Matchers.hasItem("")
        );
    }

    /**
     * HttpRequest supports HTTP HEAD method.
     * @throws Exception If something goes wrong
     */
    @Test
    void supportsHeadMethod() throws Exception {
        final HttpRequest request = HttpRequestMocker.toRequest(
            "HEAD /test.html HTTP/1.1\nHost:local\nAccept:text/plain\n"
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
     * HttpRequest handles invalid URI correctly.
     */
    @Test
    void handlesInvalidUriCorrectly() {
        Assertions.assertThrows(
            HttpException.class,
            () -> HttpRequestMocker.toRequest(
                "HEAD /%7B%7B%20item[' HTTP/1.1\nHost:local\nAccept:text/plain\n"
            )
        );
    }

}
