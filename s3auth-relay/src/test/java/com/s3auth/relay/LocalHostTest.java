/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.ResourceMocker;
import com.s3auth.hosts.Version;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link LocalHost}.
 * @since 0.0.1
 */
final class LocalHostTest {

    /**
     * LocalHost does not hide a regular URI.
     * @throws Exception If there is some problem inside
     */
    @Test
    void isHiddenReturnsFalseForRegularUri() throws Exception {
        MatcherAssert.assertThat(
            new LocalHost().isHidden(new URI("/some-uri")),
            Matchers.is(false)
        );
    }

    /**
     * LocalHost authorizes any credentials.
     */
    @Test
    void authorizesAnyCredentials() {
        MatcherAssert.assertThat(
            new LocalHost().authorized("user-name", "user-password"),
            Matchers.is(true)
        );
    }

    /**
     * LocalHost has a "localhost" toString.
     */
    @Test
    void hasLocalhostToString() {
        MatcherAssert.assertThat(
            new LocalHost(),
            Matchers.hasToString(Matchers.equalTo("localhost"))
        );
    }

    /**
     * LocalHost can render a simple home page.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesHomeResource() throws Exception {
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new LocalHost().fetch(URI.create("/"), Range.ENTIRE, Version.LATEST)
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
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new LocalHost().fetch(
                    URI.create("/version"), Range.ENTIRE, Version.LATEST
                )
            ),
            Matchers.equalTo(Manifests.read("S3Auth-Revision"))
        );
    }
}
