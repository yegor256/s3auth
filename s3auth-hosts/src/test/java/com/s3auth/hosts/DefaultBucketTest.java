/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
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

    @Test
    void mapsLegacyS3RegionToUsEast() {
        MatcherAssert.assertThat(
            "legacy 's3' must resolve to us-east-1",
            new DefaultBucket(
                new DomainMocker().init().withRegion("s3").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("us-east-1")
        );
    }

    @Test
    void stripsS3PrefixFromRegionalEndpoint() {
        MatcherAssert.assertThat(
            "legacy 's3-eu-west-1' must drop the s3- prefix",
            new DefaultBucket(
                new DomainMocker().init().withRegion("s3-eu-west-1").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("eu-west-1")
        );
    }

    @Test
    void stripsWebsitePrefixFromRegion() {
        MatcherAssert.assertThat(
            "legacy 's3-website-us-east-1' must drop the s3-website- prefix",
            new DefaultBucket(
                new DomainMocker().init()
                    .withRegion("s3-website-us-east-1").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("us-east-1")
        );
    }

    @Test
    void stripsAmazonawsSuffix() {
        MatcherAssert.assertThat(
            "'s3-eu-west-1.amazonaws.com' must drop the .amazonaws.com tail",
            new DefaultBucket(
                new DomainMocker().init()
                    .withRegion("s3-eu-west-1.amazonaws.com").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("eu-west-1")
        );
    }

    @Test
    void mapsBareDomainEndpointToUsEast() {
        MatcherAssert.assertThat(
            "'s3.amazonaws.com' must collapse to us-east-1",
            new DefaultBucket(
                new DomainMocker().init().withRegion("s3.amazonaws.com").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("us-east-1")
        );
    }

    @Test
    void mapsExternalAliasToUsEast() {
        MatcherAssert.assertThat(
            "'s3-external-1' is the legacy alias for us-east-1",
            new DefaultBucket(
                new DomainMocker().init().withRegion("s3-external-1").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("us-east-1")
        );
    }

    @Test
    void preservesAlreadyValidRegionId() {
        MatcherAssert.assertThat(
            "valid SDK v2 region must pass through unchanged",
            new DefaultBucket(
                new DomainMocker().init().withRegion("eu-central-1").mock()
            ).client().serviceClientConfiguration().region().id(),
            Matchers.equalTo("eu-central-1")
        );
    }
}
