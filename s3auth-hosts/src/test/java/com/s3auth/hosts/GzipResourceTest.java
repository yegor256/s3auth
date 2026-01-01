/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link GzipResource}.
 * @since 0.0.1
 */
final class GzipResourceTest {

    /**
     * GzipResource writes gzip compressed output.
     * @throws Exception If something goes wrong.
     */
    @Test
    void compressesOutput() throws Exception {
        final String text = "Hello compressed!\u00ac";
        final Resource res = new GzipResource(
            new Resource.PlainText(text)
        );
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        res.writeTo(out);
        MatcherAssert.assertThat(
            IOUtils.toString(
                new GZIPInputStream(
                    new ByteArrayInputStream(out.toByteArray())
                ),
                StandardCharsets.UTF_8
            ),
            Matchers.is(text)
        );
    }

    /**
     * GzipResource returns Content-Encoding header.
     * @throws Exception If something goes wrong.
     */
    @Test
    void containsContentEncodingHeader() throws Exception {
        MatcherAssert.assertThat(
            new GzipResource(new Resource.PlainText("foo")).headers(),
            Matchers.hasItem("Content-Encoding: gzip")
        );
    }

}
