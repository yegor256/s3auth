/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link HttpRequest}.
 * @since 0.0.1
 */
final class HttpRequestTest {

    /**
     * HttpRequest can parse the request URI.
     * @throws Exception If there is some problem inside
     */
    @Test
    void parsesRequestUri() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain", "", "body"
                )
            ).requestUri().toString(),
            Matchers.equalTo("/test.html")
        );
    }

    /**
     * HttpRequest can parse the Host header.
     * @throws Exception If there is some problem inside
     */
    @Test
    void parsesHostHeader() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain", "", "body"
                )
            ).headers().get("Host"),
            Matchers.hasItem("local")
        );
    }

    /**
     * HttpRequest can retrieve a header by its original case.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesHeaderByOriginalCase() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain", "", "body"
                )
            ).headers().get("Accept"),
            Matchers.hasItem("text/plain")
        );
    }

    /**
     * HttpRequest can retrieve a header by its uppercase name.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesHeaderByUppercaseName() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain", "", "body"
                )
            ).headers().get("ACCEPT"),
            Matchers.hasItem("text/plain")
        );
    }

    /**
     * HttpRequest can retrieve a header by its lowercase name.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesHeaderByLowercaseName() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain", "", "body"
                )
            ).headers().get("accept"),
            Matchers.hasItem("text/plain")
        );
    }

    /**
     * HttpRequest can retrieve a header by its mixed-case name.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesHeaderByMixedCaseName() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain", "", "body"
                )
            ).headers().get("aCcEpT"),
            Matchers.hasItem("text/plain")
        );
    }

    /**
     * HttpRequest can retrieve the first byte of a full range header value.
     * @throws Exception If a problem occurs
     */
    @Test
    void fetchesFullByteRangeFirstByte() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain",
                    "Range: bytes=100-200", "", "body"
                )
            ).range().first(),
            Matchers.is(100L)
        );
    }

    /**
     * HttpRequest can retrieve the last byte of a full range header value.
     * @throws Exception If a problem occurs
     */
    @Test
    void fetchesFullByteRangeLastByte() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain",
                    "Range: bytes=100-200", "", "body"
                )
            ).range().last(),
            Matchers.is(200L)
        );
    }

    /**
     * HttpRequest can retrieve the first byte of a range with only the
     * first byte specified, e.g. "byte=100-" for "From byte 100".
     * @throws Exception If a problem occurs
     */
    @Test
    void fetchesRangeFromFirstByteStart() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain",
                    "Range: bytes=100-", "", "body"
                )
            ).range().first(),
            Matchers.is(100L)
        );
    }

    /**
     * HttpRequest defaults the last byte to the maximum long value when
     * only the first byte is specified, e.g. "byte=100-" for "From byte 100".
     * @throws Exception If a problem occurs
     */
    @Test
    void fetchesRangeFromFirstByteEnd() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html HTTP/1.1", "Host:local",
                    "Accept:text/plain",
                    "Range: bytes=100-", "", "body"
                )
            ).range().last(),
            Matchers.is(Long.MAX_VALUE)
        );
    }

    /**
     * HttpRequest can retrieve the "test" query parameter.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesTestQueryParam() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?test=param&hello=world HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("test"),
            Matchers.hasItem("param")
        );
    }

    /**
     * HttpRequest can retrieve the "hello" query parameter.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesHelloQueryParam() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?test=param&hello=world HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("hello"),
            Matchers.hasItem("world")
        );
    }

    /**
     * HttpRequest can retrieve duplicate query parameter values.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesDuplicateQueryParamValues() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?first=one&second=two&first=three HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("first"),
            Matchers.allOf(
                Matchers.iterableWithSize(2),
                Matchers.hasItems("one", "three")
            )
        );
    }

    /**
     * HttpRequest can retrieve a non-duplicate query parameter alongside
     * duplicate ones.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesNonDuplicateQueryParam() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?first=one&second=two&first=three HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("second"),
            Matchers.hasItem("two")
        );
    }

    /**
     * HttpRequest can retrieve a query parameter with no specified value
     * at the start of the query string.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesBlankLeadingQueryParam() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?blank&something=yes&nothing HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("blank"),
            Matchers.hasItem("")
        );
    }

    /**
     * HttpRequest can retrieve a query parameter with a specified value,
     * among others with no value.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesQueryParamWithValue() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?blank&something=yes&nothing HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("something"),
            Matchers.hasItem("yes")
        );
    }

    /**
     * HttpRequest can retrieve a query parameter with no specified value
     * at the end of the query string.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesBlankTrailingQueryParam() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "GET /test.html?blank&something=yes&nothing HTTP/1.1",
                    "Host:local", "Accept:text/plain", "", "body"
                )
            ).parameters().get("nothing"),
            Matchers.hasItem("")
        );
    }

    /**
     * HttpRequest parses the request URI for HTTP HEAD method requests.
     * @throws Exception If something goes wrong
     */
    @Test
    void headMethodParsesRequestUri() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "HEAD /test.html HTTP/1.1", "Host:local", "Accept:text/plain", ""
                )
            ).requestUri().toString(),
            Matchers.equalTo("/test.html")
        );
    }

    /**
     * HttpRequest parses the Host header for HTTP HEAD method requests.
     * @throws Exception If something goes wrong
     */
    @Test
    void headMethodParsesHostHeader() throws Exception {
        MatcherAssert.assertThat(
            HttpRequestMocker.toRequest(
                HttpRequestTest.text(
                    "HEAD /test.html HTTP/1.1", "Host:local", "Accept:text/plain", ""
                )
            ).headers().get("Host"),
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
                HttpRequestTest.text(
                    "HEAD /%7B%7B%20item[' HTTP/1.1", "Host:local",
                    "Accept:text/plain", ""
                )
            )
        );
    }

    private static String text(final String... lines) {
        return String.join(System.lineSeparator(), lines);
    }
}
