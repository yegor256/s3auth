/*
 * Copyright (c) 2012-2024, Yegor Bugayenko
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
