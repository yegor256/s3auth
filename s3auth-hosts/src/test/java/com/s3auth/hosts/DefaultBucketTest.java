/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DefaultBucket}.
 *
 * @since 0.0.1
 */
final class DefaultBucketTest {

    /**
     * DefaultBucket can reproduce properties.
     */
    @Test
    void reproducesPropertiesOfDomain() {
        final Domain domain = new DomainMocker().init().mock();
        MatcherAssert.assertThat(
            new DefaultBucket(domain).name(),
            Matchers.equalTo(domain.name())
        );
    }

}
