/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.google.common.collect.ImmutableList;
import com.rexsl.test.XhtmlMatchers;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ObjectVersion;

/**
 * Test case for {@link ObjectVersionListing}.
 * @since 0.0.1
 */
final class ObjectVersionListingTest {
    /**
     * Fetches version listing for bucket.
     * @throws Exception If something goes wrong
     */
    @Test
    void fetchesVersionListingInXml() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final String[] versions = {"abc", "def", "ghi"};
        final ImmutableList.Builder<ObjectVersion> builder =
            ImmutableList.builder();
        final String key = "README.md";
        for (final String version : versions) {
            builder.add(
                ObjectVersion.builder()
                    .key(key)
                    .versionId(version)
                    .build()
            );
        }
        Mockito.doReturn(
            ListObjectVersionsResponse.builder()
                .versions(builder.build())
                .isTruncated(false)
                .build()
        ).when(client).listObjectVersions(
            Mockito.any(ListObjectVersionsRequest.class)
        );
        MatcherAssert.assertThat(
            new String(
                ResourceMocker.toByteArray(
                    new ObjectVersionListing(client, "bucket", key)
                ),
                StandardCharsets.UTF_8
            ),
            Matchers.allOf(
                ObjectVersionListingTest.hasKeyXpath(key, versions[0]),
                ObjectVersionListingTest.hasKeyXpath(key, versions[1]),
                ObjectVersionListingTest.hasKeyXpath(key, versions[2])
            )
        );
    }

    /**
     * Get Matcher for XML version element XPath checking.
     * @param key The key
     * @param version The version
     * @return The XML element matcher
     */
    private static Matcher<String> hasKeyXpath(final String key,
        final String version) {
        return XhtmlMatchers.hasXPath(
            String.format(
                "//xhtml:a[@href=\"/%s?ver=%s\" and .=\"%s\"]",
                key, version, version
            )
        );
    }
}
