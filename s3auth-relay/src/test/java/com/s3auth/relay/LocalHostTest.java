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
package com.s3auth.relay;

import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.ResourceMocker;
import com.s3auth.hosts.Version;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link LocalHost}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class LocalHostTest {

    /**
     * LocalHost can render a simple home page.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rendersHomePage() throws Exception {
        final Host host = new LocalHost();
        MatcherAssert.assertThat(
            host.isHidden(new URI("/some-uri")),
            Matchers.is(false)
        );
        MatcherAssert.assertThat(
            host.authorized("user-name", "user-password"),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            host,
            Matchers.hasToString(Matchers.equalTo("localhost"))
        );
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                host.fetch(URI.create("/"), Range.ENTIRE, Version.LATEST)
            ),
            Matchers.notNullValue()
        );
    }

    /**
     * LocalHost can report current version.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void reportsCurrentVersion() throws Exception {
        final Host host = new LocalHost();
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                host.fetch(URI.create("/version"), Range.ENTIRE, Version.LATEST)
            ),
            Matchers.equalTo(Manifests.read("S3Auth-Revision"))
        );
    }

}
