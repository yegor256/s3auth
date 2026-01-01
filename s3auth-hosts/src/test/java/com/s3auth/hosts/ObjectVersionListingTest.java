/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.google.common.collect.ImmutableList;
import com.rexsl.test.XhtmlMatchers;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final VersionListing listing = Mockito.mock(VersionListing.class);
        Mockito.doReturn(listing).when(client)
            .listVersions(Mockito.any(ListVersionsRequest.class));
        final String[] versions = {"abc", "def", "ghi"};
        final ImmutableList.Builder<S3VersionSummary> builder =
            ImmutableList.builder();
        final String key = "README.md";
        for (final String version : versions) {
            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
            final S3VersionSummary summary = new S3VersionSummary();
            summary.setKey(key);
            summary.setVersionId(version);
            builder.add(summary);
        }
        Mockito.doReturn(builder.build()).when(listing).getVersionSummaries();
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
