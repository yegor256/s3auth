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
 * Test case for {@link RejectingHost}.
 * @since 0.0.1
 */
final class RejectingHostTest {

    /**
     * RejectingHost can reject by regex.
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsByRegex() throws Exception {
        final URI uri = new URI("/test/me.txt");
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new RejectingHost(
                    new HostMocker().init().withContent(uri, "test me").mock(),
                    "/test/.*"
                ).fetch(uri, Range.ENTIRE, Version.LATEST)
            ),
            Matchers.equalTo("your resource it temporary disabled, sorry")
        );
    }

}
