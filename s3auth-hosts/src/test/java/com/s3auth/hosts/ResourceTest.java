/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Resource}.
 * @since 0.0.1
 */
final class ResourceTest {

    /**
     * Resource.PlainText can return text content.
     * @throws Exception If there is some problem inside
     */
    @Test
    void returnsPlainTextContent() throws Exception {
        final String content = "\u0433 test!";
        final Resource res = new Resource.PlainText(content);
        MatcherAssert.assertThat(
            ResourceMocker.toString(res),
            Matchers.equalTo(content)
        );
    }

    /**
     * Resource.PlainText can produce correct HTTP headers.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsHeadersForPlainText() throws Exception {
        final Resource res = new Resource.PlainText("");
        MatcherAssert.assertThat(
            res.headers(),
            Matchers.allOf(
                Matchers.hasItem("Content-Length: 0"),
                Matchers.hasItem("Content-Type: text/plain")
            )
        );
    }

    /**
     * Resource.PlainText can convert itselt to string.
     */
    @Test
    void convertsItselfToString() {
        final String content = "\u0444\u0433 test!";
        MatcherAssert.assertThat(
            new Resource.PlainText(content),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * Resource.PlainText can obtain its last modified time.
     */
    @Test
    void getsLastModifiedTime() {
        MatcherAssert.assertThat(
            new Resource.PlainText("blah").lastModified(),
            Matchers.notNullValue()
        );
    }

    /**
     * Resource.PlainText rejects null contents.
     */
    @Disabled
    @Test
    void rejectsNullContent() {
        Assertions.assertThrows(
            ConstraintViolationException.class,
            () -> new Resource.PlainText(null)
        );
    }

}
