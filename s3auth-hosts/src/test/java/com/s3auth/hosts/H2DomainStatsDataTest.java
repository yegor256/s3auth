/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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

import com.jcabi.aspects.Tv;
import java.io.File;
import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link H2DomainStatsData}.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
public final class H2DomainStatsDataTest {

    /**
     * H2DomainStatsData can put and get data for a single domain.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void putsAndGetsDataPerDomain() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("test", "temp")
        );
        final String domain = "test-put-domain";
        final long bytes = Tv.HUNDRED;
        data.put(
            domain,
            new Stats() {
                @Override
                public long bytesTransferred() {
                    return bytes;
                }
            }
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

    /**
     * H2DomainStatsData can put and get data for all domains.
     * @throws Exception If something goes wrong.
     */
    @Test
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public void getsDataForAllDomains() throws Exception {
        final H2DomainStatsData data = new H2DomainStatsData(
            File.createTempFile("testAll", "tempAll")
        );
        final String first = "test-put-domain1";
        final String second = "test-put-domain2";
        data.put(
            first,
            new Stats() {
                @Override
                public long bytesTransferred() {
                    return Tv.HUNDRED;
                }
            }
        );
        data.put(
            first,
            new Stats() {
                @Override
                public long bytesTransferred() {
                    return Tv.FIFTY;
                }
            }
        );
        data.put(
            second,
            new Stats() {
                @Override
                public long bytesTransferred() {
                    return Tv.THOUSAND;
                }
            }
        );
        final Map<String, Stats> stats = data.all();
        MatcherAssert.assertThat(
            stats.size(), Matchers.is(2)
        );
        MatcherAssert.assertThat(
            stats.get(first).bytesTransferred(),
            Matchers.is((long) (Tv.HUNDRED + Tv.FIFTY))
        );
        MatcherAssert.assertThat(
            stats.get(second).bytesTransferred(),
            Matchers.is((long) Tv.THOUSAND)
        );
        MatcherAssert.assertThat(
            data.all().size(),
            Matchers.is(0)
        );
    }

}
