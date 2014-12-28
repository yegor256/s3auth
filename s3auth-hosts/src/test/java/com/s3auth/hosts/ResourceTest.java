/**
 * Copyright (c) 2012-2014, s3auth.com
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

import javax.validation.ConstraintViolationException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link Resource}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class ResourceTest {

    /**
     * Resource.PlainText can return text content.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void returnsPlainTextContent() throws Exception {
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
    public void getsHeadersForPlainText() throws Exception {
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
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItselfToString() throws Exception {
        final String content = "\u0444\u0433 test!";
        MatcherAssert.assertThat(
            new Resource.PlainText(content),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * Resource.PlainText can obtain its last modified time.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void getsLastModifiedTime() throws Exception {
        MatcherAssert.assertThat(
            new Resource.PlainText("blah").lastModified(),
            Matchers.notNullValue()
        );
    }

    /**
     * Resource.PlainText rejects null contents.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = ConstraintViolationException.class)
    public void rejectsNullContent() throws Exception {
        new Resource.PlainText(null);
    }
}
