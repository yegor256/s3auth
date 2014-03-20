/**
 * Copyright (c) 2012, s3auth.com
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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.google.common.collect.ImmutableList;
import com.rexsl.test.XhtmlMatchers;
import org.apache.commons.io.Charsets;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ObjectVersionListing}.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
public final class ObjectVersionListingTest {
    /**
     * Fetches version listing for bucket.
     * @throws Exception If something goes wrong
     */
    @Test
    public void fetchesVersionListingInXml() throws Exception {
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
                Charsets.UTF_8
            ),
            Matchers.allOf(
                XhtmlMatchers.hasXPath(
                    String.format("/versions[@object=\"%s\"]", key)
                ),
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
                "/versions/version[@key=\"%s\" and .=\"%s\"]", key, version
            )
        );
    }
}
