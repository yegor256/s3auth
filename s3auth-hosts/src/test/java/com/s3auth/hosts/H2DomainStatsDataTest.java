/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.File;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link H2DomainStatsData}.
 * @since 0.0.1
 */
final class H2DomainStatsDataTest {

    @Test
    void getsDataJustPutForDomain() throws Exception {
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
    }

    @Test
    void clearsDataForDomainAfterGet() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("test", "temp")
        ).init();
        final String domain = "test-put-domain";
        data.put(
            domain,
            () -> 100
        );
        data.get(domain);
        MatcherAssert.assertThat(
            data.get(domain).bytesTransferred(),
            Matchers.is(0L)
        );
    }

    @Test
    void getsAllReturnsSizeEqualToDomainCount() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("testAll", "tempAll")
        ).init();
        data.put("test-put-domain1", () -> 100);
        data.put("test-put-domain1", () -> 50);
        data.put("test-put-domain2", () -> 1000);
        MatcherAssert.assertThat(
            data.all().size(), Matchers.is(2)
        );
    }

    @Test
    void getsAllSumsBytesPerFirstDomain() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("testAll", "tempAll")
        ).init();
        final String first = "test-put-domain1";
        data.put(first, () -> 100);
        data.put(first, () -> 50);
        data.put("test-put-domain2", () -> 1000);
        MatcherAssert.assertThat(
            data.all().get(first).bytesTransferred(),
            Matchers.is(150L)
        );
    }

    @Test
    void getsAllSumsBytesPerSecondDomain() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("testAll", "tempAll")
        ).init();
        final String second = "test-put-domain2";
        data.put("test-put-domain1", () -> 100);
        data.put("test-put-domain1", () -> 50);
        data.put(second, () -> 1000);
        MatcherAssert.assertThat(
            data.all().get(second).bytesTransferred(),
            Matchers.is(1000L)
        );
    }

    @Test
    void getsAllClearsDataAfterRetrieval() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("testAll", "tempAll")
        ).init();
        data.put("test-put-domain1", () -> 100);
        data.put("test-put-domain2", () -> 1000);
        data.all();
        MatcherAssert.assertThat(
            data.all().size(),
            Matchers.is(0)
        );
    }
}
