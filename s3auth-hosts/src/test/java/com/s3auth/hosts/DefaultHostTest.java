/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ListVersionsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.S3VersionSummary;
import com.amazonaws.services.s3.model.VersionListing;
import com.rexsl.test.XhtmlMatchers;
import com.s3auth.hosts.Host.CloudWatch;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link DefaultHost}.
 * @since 0.0.1
 */
@SuppressWarnings({ "PMD.ExcessiveImports", "PMD.TooManyMethods" })
final class DefaultHostTest {

    /**
     * DefaultHost can load resource from S3.
     * @throws Exception If there is some problem inside
     */
    @Test
    void loadsAmazonResourcesFrom() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doAnswer(
            (Answer<S3Object>) invocation -> {
                final String key = GetObjectRequest.class.cast(
                    invocation.getArguments()[0]
                ).getKey();
                if (key.matches(".*dir/?$")) {
                    throw new AmazonClientException(
                        String.format("%s not found", key)
                    );
                }
                final S3Object object = new S3Object();
                object.setObjectContent(IOUtils.toInputStream(key, StandardCharsets.UTF_8));
                object.setKey(key);
                return object;
            }
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        final String suffix = "index.htm";
        Mockito.doReturn(new BucketWebsiteConfiguration(suffix))
            .when(aws).getBucketWebsiteConfiguration(Mockito.anyString());
        final Host host = new DefaultHost(
            new BucketMocker().withClient(aws).mock(), this.cloudWatch()
        );
        @SuppressWarnings("PMD.NonStaticInitializer")
        final ConcurrentMap<String, String> paths =
            new ConcurrentHashMap<String, String>() {
                private static final long serialVersionUID = 1L;
                {
                    this.put("/test/a/a/alpha.html?q", "test/a/a/alpha.html");
                    this.put("", suffix);
                    this.put("/", suffix);
                    this.put("/dir/index.html", "dir/index.html");
                    this.put("/dir/", String.format("dir/%s", suffix));
                }
            };
        for (final Map.Entry<String, String> path : paths.entrySet()) {
            MatcherAssert.assertThat(
                ResourceMocker.toString(
                    host.fetch(
                        URI.create(path.getKey()), Range.ENTIRE, Version.LATEST
                    )
                ),
                Matchers.equalTo(path.getValue())
            );
        }
    }

    /**
     * DefaultHost can show some stats in {@code #toString()}.
     */
    @Test
    void showsStatsInToString() {
        MatcherAssert.assertThat(
            new DefaultHost(new BucketMocker().mock()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * DefaultHost can reject authorization with invalid credentials.
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsAuthorizationWhenInvalidCredentials() throws Exception {
        MatcherAssert.assertThat(
            new DefaultHost(
                new BucketMocker().mock(), this.cloudWatch()
            ).authorized("1", "2"),
            Matchers.is(false)
        );
    }

    /**
     * DefaultHost can throw a specific exception for a non existent bucket.
     *
     * @see <a href="http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html">S3 Error Responses</a>
     */
    @Test
    void throwsExceptionForNonexistentBucket() {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        final AmazonServiceException exp =
            new AmazonServiceException("No such bucket");
        exp.setErrorCode("NoSuchBucket");
        Mockito.doThrow(exp)
            .when(aws).getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(new BucketWebsiteConfiguration())
            .when(aws).getBucketWebsiteConfiguration(Mockito.anyString());
        final String bucket = "nonExistent";
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IOException.class,
                () -> new DefaultHost(
                    new BucketMocker().withBucket(bucket).withClient(aws).mock(),
                    this.cloudWatch()
                ).fetch(URI.create("/.htpasswd"), Range.ENTIRE, Version.LATEST)
            ).getMessage(),
            Matchers.allOf(
                Matchers.not(Matchers.startsWith("failed to fetch /.htpasswd")),
                Matchers.is(
                    String.format("The bucket '%s' does not exist.", bucket)
                )
            )
        );
    }

    /**
     * DefaultHost can return a directory listing when the resource key does
     * not exist and ends with "index.html".
     * @throws Exception If a problem occurs.
     */
    @Test
    void showsDirectoryListing() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final ObjectListing listing = Mockito.mock(ObjectListing.class);
        final S3ObjectSummary summary = new S3ObjectSummary();
        final String name = "foo/bar/boo";
        summary.setKey(name);
        Mockito.doReturn(Collections.singletonList(summary))
            .when(listing).getObjectSummaries();
        final AmazonServiceException exc =
            new AmazonServiceException("No such key.");
        exc.setErrorCode("NoSuchKey");
        Mockito.doThrow(exc).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(listing).when(client)
            .listObjects(Mockito.any(ListObjectsRequest.class));
        final String key = "foo/bar/index.html";
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultHost(
                    new BucketMocker().withClient(client).mock(),
                    this.cloudWatch()
                ).fetch(new URI(key), Range.ENTIRE, Version.LATEST)
            ),
            XhtmlMatchers.hasXPath(
                String.format(
                    "//xhtml:a[@href=\"/%s\" and .=\"%s\"]", name, name
                )
            )
        );
    }

    /**
     * DefaultHost can return a version listing.
     * @throws Exception If a problem occurs.
     */
    @Test
    void showsVersionListing() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final VersionListing listing = Mockito.mock(VersionListing.class);
        final S3VersionSummary summary = new S3VersionSummary();
        final String key = "README.md";
        summary.setKey(key);
        summary.setVersionId("abc");
        Mockito.doReturn(Collections.singletonList(summary))
            .when(listing).getVersionSummaries();
        Mockito.doReturn(listing).when(client)
            .listVersions(Mockito.any(ListVersionsRequest.class));
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultHost(
                    new BucketMocker().withClient(client).mock(),
                    this.cloudWatch()
                ).fetch(new URI(key), Range.ENTIRE, Version.LIST)
            ),
            XhtmlMatchers.hasXPaths(
                "//xhtml:a[@href=\"/README.md?ver=abc\" and .=\"abc\"]"
            )
        );
    }

    /**
     * DefaultHost can correctly return index.html version listing.
     * @throws Exception If a problem occurs.
     */
    @Test
    void showsVersionListingForIndexHtml() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final VersionListing listing = Mockito.mock(VersionListing.class);
        final S3VersionSummary summary = new S3VersionSummary();
        final String key = "hello/index.html";
        summary.setKey(key);
        summary.setVersionId("def");
        Mockito.doReturn(Collections.singletonList(summary))
            .when(listing).getVersionSummaries();
        Mockito.doReturn(listing).when(client)
            .listVersions(Mockito.any(ListVersionsRequest.class));
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultHost(
                    new BucketMocker().withClient(client).mock(),
                    this.cloudWatch()
                ).fetch(new URI(key), Range.ENTIRE, Version.LIST)
            ),
            XhtmlMatchers.hasXPaths(
                "//xhtml:a[@href=\"/hello/index.html?ver=def\" and .=\"def\"]"
            )
        );
    }

    /**
     * DefaultHost can retrieve Cloudwatch stats and cache the results.
     */
    @Test
    void retrievesAndCachesCloudWatchStats() {
        final long sum = 10;
        final CloudWatch cloudwatch = this.cloudWatch();
        final GetMetricStatisticsResult result = new GetMetricStatisticsResult()
            .withDatapoints(new Datapoint().withSum(Double.valueOf(sum)));
        Mockito.doReturn(result).when(cloudwatch.get())
            .getMetricStatistics(Mockito.any(GetMetricStatisticsRequest.class));
        MatcherAssert.assertThat(
            new DefaultHost(
                new BucketMocker().mock(),
                cloudwatch
            ).stats().bytesTransferred(),
            Matchers.allOf(
                Matchers.is(sum),
                Matchers.is(
                    new DefaultHost(
                        new BucketMocker().mock(),
                        cloudwatch
                    ).stats().bytesTransferred()
                )
            )
        );
        Mockito.verify(cloudwatch.get(), Mockito.times(1)).getMetricStatistics(
            Mockito.any(GetMetricStatisticsRequest.class)
        );
    }

    /**
     * DefaultHost can load error document from S3 if status code is 4xx.
     * @throws Exception If there is some problem inside
     */
    @Test
    void loadsErrorDocument() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        final String suffix = "nonExistent.html";
        final String error = "error.html";
        final String message = "Test output for error page";
        Mockito.doAnswer(
            (Answer<S3Object>) invocation -> {
                final String key = GetObjectRequest.class.cast(
                    invocation.getArguments()[0]
                ).getKey();
                if (key.endsWith(suffix)) {
                    final AmazonServiceException ex =
                        new AmazonServiceException("Object not found");
                    ex.setStatusCode(HttpStatus.SC_NOT_FOUND);
                    throw ex;
                }
                MatcherAssert.assertThat(key, Matchers.is(error));
                final S3Object object = new S3Object();
                object.setObjectContent(IOUtils.toInputStream(message, StandardCharsets.UTF_8));
                object.setKey(message);
                return object;
            }
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(
            new BucketWebsiteConfiguration(suffix, error)
        ).when(aws).getBucketWebsiteConfiguration(Mockito.anyString());
        final Host host = new DefaultHost(
            new BucketMocker().withClient(aws).mock(), this.cloudWatch()
        );
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                host.fetch(
                    URI.create(suffix), Range.ENTIRE, Version.LATEST
                )
            ),
            Matchers.equalTo(message)
        );
    }

    @Test
    public void throwsExceptionIfNoBucketWebsiteConfiguration()
        throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        final AmazonServiceException ex =
            new AmazonServiceException("The object is not found");
        ex.setStatusCode(HttpStatus.SC_NOT_FOUND);
        Mockito.doThrow(ex).when(aws)
            .getObject(Mockito.any(GetObjectRequest.class));
        Assertions.assertThrows(
            IOException.class,
            () -> new DefaultHost(
                new BucketMocker().withClient(aws).mock(), this.cloudWatch()
            ).fetch(URI.create("failed.html"), Range.ENTIRE, Version.LATEST)
        );
    }

    /**
     * Mock Host.Cloudwatch for test.
     * @return Mock Cloudwatch
     */
    private CloudWatch cloudWatch() {
        return new Host.CloudWatch() {
            /**
             * Mock Cloudwatch client.
             */
            private final transient AmazonCloudWatchClient cwatch =
                Mockito.mock(AmazonCloudWatchClient.class);
            @Override
            public AmazonCloudWatchClient get() {
                return this.cwatch;
            }
        };
    }

}
