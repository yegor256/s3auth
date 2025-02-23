/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.collect.ImmutableList;
import com.rexsl.test.XhtmlMatchers;
import java.nio.charset.StandardCharsets;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DirectoryListing}.
 * @since 0.0.1
 */
final class DirectoryListingTest {
    /**
     * Fetches directory listing for bucket, if object does not exist.
     * @throws Exception If something goes wrong
     */
    @Test
    void fetchesDirectoryListingInXhtml()
        throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final ObjectListing listing = Mockito.mock(ObjectListing.class);
        Mockito.doReturn(listing).when(client)
            .listObjects(Mockito.any(ListObjectsRequest.class));
        final String[] prefixes = {"baz/", "biz/", "boz/"};
        Mockito.doReturn(ImmutableList.copyOf(prefixes)).when(listing)
            .getCommonPrefixes();
        final String[] names = {"baa.txt", "bee.jpg", "boo.png"};
        final ImmutableList.Builder<S3ObjectSummary> builder =
            ImmutableList.builder();
        for (final String key : names) {
            @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
            final S3ObjectSummary summary = new S3ObjectSummary();
            summary.setKey(key);
            summary.setSize(10);
            builder.add(summary);
        }
        Mockito.doReturn(builder.build()).when(listing).getObjectSummaries();
        final String prefix = "foo/bar/";
        MatcherAssert.assertThat(
            new String(
                ResourceMocker.toByteArray(
                    new DirectoryListing(client, "bucket", prefix)
                ),
                StandardCharsets.UTF_8
            ),
            Matchers.allOf(
                hasCommonPrefix(prefixes[0]),
                hasCommonPrefix(prefixes[1]),
                hasObject(names[0], 10),
                hasObject(names[1], 10),
                hasObject(names[2], 10)
            )
        );
    }

    /**
     * Get Matcher for object element checking.
     * @param key The key
     * @param size The size
     * @return Matcher for object element
     */
    private static Matcher<String> hasObject(final String key, final int size) {
        return XhtmlMatchers.hasXPaths(
            String.format(
                "//xhtml:a[@href=\"/%s\" and .=\"%s\"]",
                key, key
            ),
            String.format("//xhtml:td[.=\"%d\"]", size)
        );
    }

    /**
     * Get Matcher for XML commonPrefix element XPath checking.
     * @param prefix The key
     * @return Matcher for common prefix element
     */
    private static Matcher<String> hasCommonPrefix(final String prefix) {
        return XhtmlMatchers.hasXPath(
            String.format(
                "//xhtml:a[@href=\"/%sindex.html\" and .=\"%s\"]",
                prefix, prefix
            )
        );
    }
}
