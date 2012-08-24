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
package com.s3auth.hosts;

import com.rexsl.core.Manifests;
import java.net.URI;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link DefaultHost}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DefaultHostTest {

    /**
     * DefaultHost can load resource from S3.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void loadsAmazonResources() throws Exception {
        final Host host = new DefaultHost(
            new DomainMocker()
                .withName("junit.s3auth.com")
                .withKey(Manifests.read("S3Auth-AwsDynamoKey"))
                .withSecret(Manifests.read("S3Auth-AwsDynamoSecret"))
                .mock()
        );
        MatcherAssert.assertThat(
            IOUtils.toString(host.fetch(URI.create("/index.html"))),
            Matchers.equalTo("<html>hello</html>")
        );
        MatcherAssert.assertThat(
            IOUtils.toString(host.fetch(URI.create("/"))),
            Matchers.startsWith("<html>hello")
        );
        MatcherAssert.assertThat(
            IOUtils.toString(host.fetch(URI.create("/foo/index.html"))),
            Matchers.equalTo("<html>bye</html>")
        );
    }

    /**
     * DefaultHost can throw IOException for absent object.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void throwsWhenAbsentResource() throws Exception {
        final Host host = new DefaultHost(
            new DomainMocker()
                .withName("invalid-bucket.s3auth.com")
                .withKey("foo")
                .withSecret("invalid-data")
                .mock()
        );
        host.fetch(URI.create("foo.html"));
    }

}
