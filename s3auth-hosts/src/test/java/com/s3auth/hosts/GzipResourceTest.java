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
package com.s3auth.hosts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link GzipResource}.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
public final class GzipResourceTest {

    /**
     * GzipResource writes gzip compressed output.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void compressesOutput() throws Exception {
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
    public void containsContentEncodingHeader() throws Exception {
        MatcherAssert.assertThat(
            new GzipResource(new Resource.PlainText("foo")).headers(),
            Matchers.hasItem("Content-Encoding: gzip")
        );
    }

}
