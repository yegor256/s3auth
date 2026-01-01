/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link LocalHost}.
 *
 * @since 0.0.1
 */
final class LocalHostTest {

    /**
     * LocalHost can render a simple home page.
     * @throws Exception If there is some problem inside
     */
    @Test
    void rendersHomePage() throws Exception {
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
    void reportsCurrentVersion() throws Exception {
        final Host host = new LocalHost();
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                host.fetch(URI.create("/version"), Range.ENTIRE, Version.LATEST)
            ),
            Matchers.equalTo(Manifests.read("S3Auth-Revision"))
        );
    }

}
