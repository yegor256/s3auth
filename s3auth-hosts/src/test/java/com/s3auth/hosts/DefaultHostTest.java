/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.rexsl.test.XhtmlMatchers;
import com.s3auth.hosts.Host.CloudWatch;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ErrorDocument;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.IndexDocument;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectVersionsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectVersion;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;

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
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doAnswer(
            (Answer<ResponseInputStream<GetObjectResponse>>) invocation -> {
                final GetObjectRequest req = (GetObjectRequest) invocation.getArguments()[0];
                final String key = req.key();
                if (key.matches(".*dir/?$")) {
                    throw NoSuchKeyException.builder()
                        .message(String.format("%s not found", key))
                        .build();
                }
                final byte[] data = key.getBytes(StandardCharsets.UTF_8);
                return new ResponseInputStream<>(
                    GetObjectResponse.builder()
                        .contentLength((long) data.length)
                        .build(),
                    AbortableInputStream.create(new ByteArrayInputStream(data))
                );
            }
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        final String suffix = "index.htm";
        Mockito.doReturn(
            GetBucketWebsiteResponse.builder()
                .indexDocument(IndexDocument.builder().suffix(suffix).build())
                .build()
        ).when(aws).getBucketWebsite(
            Mockito.any(GetBucketWebsiteRequest.class)
        );
        final Host host = new DefaultHost(
            new BucketMocker().init().withClient(aws).mock(), this.cloudWatch()
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
            new DefaultHost(new BucketMocker().init().mock()),
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
                new BucketMocker().init().mock(), this.cloudWatch()
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
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doThrow(
            NoSuchBucketException.builder()
                .message("No such bucket")
                .build()
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(
            GetBucketWebsiteResponse.builder().build()
        ).when(aws).getBucketWebsite(
            Mockito.any(GetBucketWebsiteRequest.class)
        );
        final String bucket = "nonExistent";
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IOException.class,
                () -> new DefaultHost(
                    new BucketMocker().init().withBucket(bucket).withClient(aws).mock(),
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
        final S3Client client = Mockito.mock(S3Client.class);
        final String name = "foo/bar/boo";
        final S3Object summary = S3Object.builder().key(name).size(0L).build();
        Mockito.doReturn(
            ListObjectsResponse.builder()
                .contents(Collections.singletonList(summary))
                .isTruncated(false)
                .build()
        ).when(client).listObjects(Mockito.any(ListObjectsRequest.class));
        Mockito.doThrow(
            NoSuchKeyException.builder()
                .message("No such key")
                .build()
        ).when(client).getObject(Mockito.any(GetObjectRequest.class));
        final String key = "foo/bar/index.html";
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultHost(
                    new BucketMocker().init().withClient(client).mock(),
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
        final S3Client client = Mockito.mock(S3Client.class);
        final String key = "README.md";
        final ObjectVersion summary = ObjectVersion.builder()
            .key(key)
            .versionId("abc")
            .build();
        Mockito.doReturn(
            ListObjectVersionsResponse.builder()
                .versions(Collections.singletonList(summary))
                .isTruncated(false)
                .build()
        ).when(client).listObjectVersions(
            Mockito.any(ListObjectVersionsRequest.class)
        );
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultHost(
                    new BucketMocker().init().withClient(client).mock(),
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
        final S3Client client = Mockito.mock(S3Client.class);
        final String key = "hello/index.html";
        final ObjectVersion summary = ObjectVersion.builder()
            .key(key)
            .versionId("def")
            .build();
        Mockito.doReturn(
            ListObjectVersionsResponse.builder()
                .versions(Collections.singletonList(summary))
                .isTruncated(false)
                .build()
        ).when(client).listObjectVersions(
            Mockito.any(ListObjectVersionsRequest.class)
        );
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultHost(
                    new BucketMocker().init().withClient(client).mock(),
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
        final GetMetricStatisticsResponse result = GetMetricStatisticsResponse.builder()
            .datapoints(Datapoint.builder().sum(Double.valueOf(sum)).build())
            .build();
        Mockito.doReturn(result).when(cloudwatch.get())
            .getMetricStatistics(Mockito.any(GetMetricStatisticsRequest.class));
        MatcherAssert.assertThat(
            new DefaultHost(
                new BucketMocker().init().mock(),
                cloudwatch
            ).stats().bytesTransferred(),
            Matchers.allOf(
                Matchers.is(sum),
                Matchers.is(
                    new DefaultHost(
                        new BucketMocker().init().mock(),
                        cloudwatch
                    ).stats().bytesTransferred()
                )
            )
        );
    }

    /**
     * DefaultHost can load error document from S3 if status code is 4xx.
     * @throws Exception If there is some problem inside
     */
    @Test
    void loadsErrorDocument() throws Exception {
        final S3Client aws = Mockito.mock(S3Client.class);
        final String suffix = "nonExistent.html";
        final String error = "error.html";
        final String message = "Test output for error page";
        Mockito.doAnswer(
            (Answer<ResponseInputStream<GetObjectResponse>>) invocation -> {
                final GetObjectRequest req = (GetObjectRequest) invocation.getArguments()[0];
                final String key = req.key();
                if (key.endsWith(suffix)) {
                    throw S3Exception.builder()
                        .message("Object not found")
                        .statusCode(404)
                        .build();
                }
                MatcherAssert.assertThat(key, Matchers.is(error));
                final byte[] data = message.getBytes(StandardCharsets.UTF_8);
                return new ResponseInputStream<>(
                    GetObjectResponse.builder()
                        .contentLength((long) data.length)
                        .build(),
                    AbortableInputStream.create(new ByteArrayInputStream(data))
                );
            }
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(
            GetBucketWebsiteResponse.builder()
                .indexDocument(IndexDocument.builder().suffix(suffix).build())
                .errorDocument(ErrorDocument.builder().key(error).build())
                .build()
        ).when(aws).getBucketWebsite(
            Mockito.any(GetBucketWebsiteRequest.class)
        );
        final Host host = new DefaultHost(
            new BucketMocker().init().withClient(aws).mock(), this.cloudWatch()
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
    void throwsExceptionIfNoBucketWebsiteConfiguration() {
        final S3Client aws = Mockito.mock(S3Client.class);
        Mockito.doThrow(
            S3Exception.builder()
                .message("The object is not found")
                .statusCode(404)
                .build()
        ).when(aws).getObject(Mockito.any(GetObjectRequest.class));
        Assertions.assertThrows(
            IOException.class,
            () -> new DefaultHost(
                new BucketMocker().init().withClient(aws).mock(), this.cloudWatch()
            ).fetch(URI.create("failed.html"), Range.ENTIRE, Version.LATEST)
        );
    }

    /**
     * Mock Host.Cloudwatch for test.
     * @return Mock Cloudwatch
     */
    private CloudWatch cloudWatch() {
        return new Host.CloudWatch() {
            private final transient CloudWatchClient cwatch =
                Mockito.mock(CloudWatchClient.class);

            @Override
            public CloudWatchClient get() {
                return this.cwatch;
            }
        };
    }

}
