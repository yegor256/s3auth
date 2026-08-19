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
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

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
    void fetchesDirectoryListingInXhtml() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final String[] prefixes = {"baz/", "biz/", "boz/"};
        final ImmutableList.Builder<CommonPrefix> prfxs =
            ImmutableList.builder();
        for (final String prefix : prefixes) {
            prfxs.add(CommonPrefix.builder().prefix(prefix).build());
        }
        final String[] names = {"baa.txt", "bee.jpg", "boo.png"};
        final ImmutableList.Builder<S3Object> builder =
            ImmutableList.builder();
        for (final String key : names) {
            builder.add(S3Object.builder().key(key).size(10L).build());
        }
        Mockito.doReturn(
            ListObjectsResponse.builder()
                .contents(builder.build())
                .commonPrefixes(prfxs.build())
                .isTruncated(false)
                .build()
        ).when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        final String prefix = "foo/bar/";
        MatcherAssert.assertThat(
            new String(
                ResourceMocker.toByteArray(
                    DirectoryListing.fetch(client, "bucket", prefix)
                ),
                StandardCharsets.UTF_8
            ),
            Matchers.allOf(
                matchesCommonPrefix(prefixes[0]),
                matchesCommonPrefix(prefixes[1]),
                matchesObject(names[0], 10),
                matchesObject(names[1], 10),
                matchesObject(names[2], 10)
            )
        );
    }

    /**
     * Get Matcher for object element checking.
     * @param key The key
     * @param size The size
     * @return Matcher for object element
     */
    private static Matcher<String> matchesObject(final String key, final int size) {
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
    private static Matcher<String> matchesCommonPrefix(final String prefix) {
        return XhtmlMatchers.hasXPath(
            String.format(
                "//xhtml:a[@href=\"/%sindex.html\" and .=\"%s\"]",
                prefix, prefix
            )
        );
    }
}
