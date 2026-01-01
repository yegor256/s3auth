/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.File;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link H2DomainStatsData}.
 * @since 0.0.1
 */
final class H2DomainStatsDataTest {

    @Test
    void putsAndGetsDataPerDomain() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("test", "temp")
        ).init();
        final String domain = "test-put-domain";
        final long bytes = 100;
        data.put(
            domain,
            () -> bytes
        );
        MatcherAssert.assertThat(
            data.get(domain).bytesTransferred(),
            Matchers.is(bytes)
        );
        MatcherAssert.assertThat(
            data.get(domain).bytesTransferred(),
            Matchers.is(0L)
        );
    }

    @Test
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    void getsDataForAllDomains() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("testAll", "tempAll")
        ).init();
        final String first = "test-put-domain1";
        final String second = "test-put-domain2";
        data.put(
            first,
            () -> 100
        );
        data.put(
            first,
            () -> 50
        );
        data.put(
            second,
            () -> 1000
        );
        final Map<String, Stats> stats = data.all();
        MatcherAssert.assertThat(
            stats.size(), Matchers.is(2)
        );
        MatcherAssert.assertThat(
            stats.get(first).bytesTransferred(),
            Matchers.is((long) 150)
        );
        MatcherAssert.assertThat(
            stats.get(second).bytesTransferred(),
            Matchers.is((long) 1000)
        );
        MatcherAssert.assertThat(
            data.all().size(),
            Matchers.is(0)
        );
    }

}
