/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link HttpException}.
 *
 * @since 0.0.1
 */
final class HttpExceptionTest {

    /**
     * HttpException can be instantiated with a text.
     * @throws Exception If there is some problem inside
     */
    @Test
    void parsesHttpException() throws Exception {
        final HttpException exp = new HttpException(
            HttpURLConnection.HTTP_NOT_FOUND,
            "not found"
        );
        MatcherAssert.assertThat(
            HttpResponseMocker.toString(exp.response()),
            Matchers.startsWith("HTTP/1.1 404 Not Found")
        );
    }

    /**
     * HttpException can be instantiated with a NULL inside exception.
     * @throws Exception If there is some problem inside
     */
    @Test
    void makesHttpResponseWithNull() throws Exception {
        final HttpException exp = new HttpException(
            HttpURLConnection.HTTP_SEE_OTHER,
            new IOException((String) null)
        );
        MatcherAssert.assertThat(
            HttpResponseMocker.toString(exp.response()),
            Matchers.startsWith("HTTP/1.1 303 See Other")
        );
    }

}
