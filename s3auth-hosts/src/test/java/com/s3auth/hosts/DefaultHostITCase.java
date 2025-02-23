/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.s3auth.hosts.Host.CloudWatch;
import java.io.IOException;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Integration test case for {@link DefaultHost}.
 * @since 0.0.1
 */
final class DefaultHostITCase {

    /**
     * DefaultHost can fetch a real object from S3 bucket.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesRealObjectFromAmazonBucket() throws Exception {
        final String key = System.getProperty("failsafe.aws.key");
        final String secret = System.getProperty("failsafe.aws.secret");
        Assume.assumeThat(key, Matchers.notNullValue());
        Assume.assumeThat(key.isEmpty(), Matchers.is(false));
        final Host host = new DefaultHost(
            new DefaultBucket(
                new DomainMocker().init()
                    .withName("maven.s3auth.com")
                    .withKey(key)
                    .withSecret(secret)
                    .withBucket("maven.s3auth.com")
                    .withRegion("us-east-1")
                    .mock()
            ),
            this.cloudWatch()
        );
        final Resource resource = host.fetch(
            URI.create("/index.html"), new Range.Simple(3, 500), Version.LATEST
        );
        try {
            MatcherAssert.assertThat(
                ResourceMocker.toString(resource),
                Matchers.startsWith("OCTYPE html>\n")
            );
        } finally {
            resource.close();
        }
    }

    /**
     * DefaultHost can throw IOException for absent object.
     */
    @Test
    void throwsWhenAbsentResource() {
        final Host host = new DefaultHost(
            new DefaultBucket(
                new DomainMocker().init()
                    .withName("invalid-bucket.s3auth.com")
                    .withKey("foo")
                    .withSecret("invalid-data")
                    .mock()
            ),
            this.cloudWatch()
        );
        Assertions.assertThrows(
            IOException.class,
            () -> host.fetch(URI.create("foo.html"), Range.ENTIRE, Version.LATEST)
        );
    }

    /**
     * Mock CloudWatch for test.
     *
     * @return Mock cloudwatch
     */
    private CloudWatch cloudWatch() {
        return () -> Mockito.mock(AmazonCloudWatchClient.class);
    }
}
