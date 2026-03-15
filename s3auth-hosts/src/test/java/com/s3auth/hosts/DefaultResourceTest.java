/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * Test case for {@link DefaultResource}.
 * @since 0.0.1
 */
final class DefaultResourceTest {

    /**
     * DefaultResource can build headers.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsHeadersFromAmazonObject() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(1L)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Resource res = new DefaultResource(
            client, "a", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        );
        MatcherAssert.assertThat(
            res.headers(),
            Matchers.hasItem("Content-Length: 1")
        );
    }

    /**
     * DefaultResource can write to output stream.
     * @throws Exception If there is some problem inside
     */
    @Test
    void writesFromAmazonObjectToOutputStream() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(0L)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                new DefaultResource(
                    client, "b", "", Range.ENTIRE, Version.LATEST,
                    Mockito.mock(DomainStatsData.class)
                )
            ),
            Matchers.equalTo("")
        );
    }

    /**
     * DefaultResource can write a real input stream to output stream.
     * @throws Exception If there is some problem inside
     */
    @Test
    void writesInputToOutputStream() throws Exception {
        final int size = 100 * 1024;
        final byte[] data = new byte[size];
        final Random random = new SecureRandom();
        for (int pos = 0; pos < size; ++pos) {
            data[pos] = (byte) random.nextInt();
        }
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength((long) size)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(data))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        MatcherAssert.assertThat(
            ResourceMocker.toByteArray(
                new DefaultResource(
                    client, "c", "", Range.ENTIRE, Version.LATEST,
                    Mockito.mock(DomainStatsData.class)
                )
            ),
            Matchers.equalTo(data)
        );
    }

    /**
     * DefaultResource can throw when failed to read.
     * @throws Exception If there is some problem inside
     */
    @Test
    void throwsWhenFailedToRead() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(10L)
            .build();
        final java.io.InputStream bad = new java.io.InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("oops");
            }
        };
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(bad)
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        Assertions.assertThrows(
            IOException.class,
            () -> ResourceMocker.toString(
                new DefaultResource(
                    client, "d", "", Range.ENTIRE, Version.LATEST,
                    Mockito.mock(DomainStatsData.class)
                )
            )
        );
    }

    /**
     * DefaultResource can obtain its last modified date.
     */
    @Test
    void getsLastModifiedDate() {
        final Instant date = Instant.now();
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(0L)
            .lastModified(date)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Resource res = new DefaultResource(
            client, "x", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        );
        MatcherAssert.assertThat(
            res.lastModified(),
            Matchers.is(Date.from(date))
        );
    }

    /**
     * DefaultResource can get Cache-Control info.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsCacheControlHeaderFromAmazonObject() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(0L)
            .cacheControl("max-age: 600, public")
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Resource res = new DefaultResource(
            client, "e", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        );
        MatcherAssert.assertThat(
            res.headers(),
            Matchers.hasItem("Cache-Control: max-age: 600, public")
        );
    }

    /**
     * DefaultResource can get default Cache-Control info if resource metadata
     * does not specify it.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsDefaultCacheControlHeader() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(0L)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Resource res = new DefaultResource(
            client, "f", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        );
        MatcherAssert.assertThat(
            res.headers(),
            Matchers.hasItem("Cache-Control: must-revalidate")
        );
    }

    /**
     * DefaultResource can post metrics.
     * @throws Exception If there is some problem inside
     */
    @Test
    void postsMetricData() throws Exception {
        final int size = 100;
        final byte[] data = new byte[size];
        final Random random = new Random();
        for (int pos = 0; pos < size; ++pos) {
            data[pos] = (byte) random.nextInt();
        }
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength((long) size)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(data))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final DomainStatsData stats = Mockito.mock(DomainStatsData.class);
        final String bucket = "MetricsTest";
        MatcherAssert.assertThat(
            ResourceMocker.toByteArray(
                new DefaultResource(
                    client, bucket, "", Range.ENTIRE, Version.LATEST, stats
                )
            ),
            Matchers.equalTo(data)
        );
        Mockito.verify(stats, Mockito.only()).put(
            bucket, new Stats.Simple(data.length)
        );
    }

    /**
     * DefaultResource can specify an object version to retrieve.
     */
    @Test
    void specifiesObjectVersion() {
        final S3Client client = Mockito.mock(S3Client.class);
        final String version = "abcd";
        Mockito.doAnswer(
            (Answer<ResponseInputStream<GetObjectResponse>>) invocation -> {
                final GetObjectRequest req =
                    (GetObjectRequest) invocation.getArguments()[0];
                MatcherAssert.assertThat(
                    req.versionId(), Matchers.is(version)
                );
                final GetObjectResponse response = GetObjectResponse.builder()
                    .contentLength(0L)
                    .build();
                return new ResponseInputStream<>(
                    response,
                    AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
                );
            }
        ).when(client).getObject(Mockito.any(GetObjectRequest.class));
        new DefaultResource(
            client, "h", "", Range.ENTIRE, new Version.Simple(version),
            Mockito.mock(DomainStatsData.class)
        );
    }

    /**
     * DefaultResource can close the underlying S3Object.
     * @throws Exception If there is some problem inside
     */
    @Test
    void closesUnderlyingObject() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(1L)
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            Mockito.spy(
                new ResponseInputStream<>(
                    response,
                    AbortableInputStream.create(
                        new ByteArrayInputStream(new byte[0])
                    )
                )
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        new DefaultResource(
            client, "i", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        ).close();
        Mockito.verify(stream, Mockito.times(1)).close();
    }

    /**
     * DefaultResource closes the underlying object when obtaining the full
     * object size from the Content-Range header.
     * @throws Exception If there is some problem inside
     */
    @Test
    void closesUnderlyingObjectWhenSizeIsInvoked() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        Mockito.doAnswer(
            (Answer<ResponseInputStream<GetObjectResponse>>) invocation -> {
                final GetObjectResponse response = GetObjectResponse.builder()
                    .contentLength(10L)
                    .build();
                return new ResponseInputStream<>(
                    response,
                    AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
                );
            }
        ).when(client).getObject(Mockito.any(GetObjectRequest.class));
        final Collection<String> headers = new DefaultResource(
            client, "j", "", new Range.Simple(0, 1), Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        ).headers();
        MatcherAssert.assertThat(
            headers,
            Matchers.hasItem(
                Matchers.containsString("Content-Range: bytes 0-1/10")
            )
        );
    }

    /**
     * DefaultResource can get Content-Encoding info.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsContentEncodingHeaderFromAmazonObject() throws Exception {
        final S3Client client = Mockito.mock(S3Client.class);
        final GetObjectResponse response = GetObjectResponse.builder()
            .contentLength(0L)
            .contentEncoding("gzip")
            .build();
        final ResponseInputStream<GetObjectResponse> stream =
            new ResponseInputStream<>(
                response,
                AbortableInputStream.create(new ByteArrayInputStream(new byte[0]))
            );
        Mockito.doReturn(stream).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Resource res = new DefaultResource(
            client, "abcdef", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        );
        MatcherAssert.assertThat(
            res.headers(),
            Matchers.hasItem("Content-Encoding: gzip")
        );
    }

}
