/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link SmartHost}.
 * @since 0.0.1
 */
final class SmartHostTest {

    /**
     * SmartHost can show result of .htpasswd fetching.
     * @throws Exception If there is some problem inside
     */
    @Test
    void showsLogsOfHtpasswdFetching() throws Exception {
        final URI uri = new URI("/.htpasswd");
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new SmartHost(
                    new HostMocker().init().withContent(uri, "test me").mock()
                ).fetch(uri, Range.ENTIRE, Version.LATEST)
            ),
            Matchers.equalTo("7 byte(s)")
        );
    }

    /**
     * SmartHost can convert itself to string.
     */
    @Test
    void convertsItselfToString() {
        MatcherAssert.assertThat(
            new SmartHost(new HostMocker().init().mock()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

}
