/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import org.apache.http.client.methods.HttpGet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

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
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn(1L).when(meta).getContentLength();
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
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final S3ObjectInputStream stream =
            Mockito.mock(S3ObjectInputStream.class);
        Mockito.doReturn(-1).when(stream).read(Mockito.any(byte[].class));
        Mockito.doReturn(stream).when(object).getObjectContent();
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
        final S3ObjectInputStream stream = new S3ObjectInputStream(
            new ByteArrayInputStream(data),
            new HttpGet()
        );
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(stream).when(object).getObjectContent();
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
        final S3ObjectInputStream stream =
            Mockito.mock(S3ObjectInputStream.class);
        Mockito.doThrow(new IOException("oops"))
            .when(stream).read(Mockito.any(byte[].class));
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(stream).when(object).getObjectContent();
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
        final Date date = new Date();
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn(date).when(meta).getLastModified();
        final Resource res = new DefaultResource(
            client, "x", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        );
        MatcherAssert.assertThat(
            res.lastModified(),
            Matchers.is(date)
        );
    }

    /**
     * DefaultResource can get Cache-Control info.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsCacheControlHeaderFromAmazonObject() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn("max-age: 600, public").when(meta).getCacheControl();
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
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn(null).when(meta).getCacheControl();
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
        final S3ObjectInputStream stream = new S3ObjectInputStream(
            new ByteArrayInputStream(data),
            new HttpGet()
        );
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        Mockito.doReturn(stream).when(object).getObjectContent();
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
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final String version = "abcd";
        Mockito.doAnswer(
            (Answer<S3Object>) invocation -> {
                final GetObjectRequest req =
                    (GetObjectRequest) invocation.getArguments()[0];
                MatcherAssert.assertThat(
                    req.getVersionId(), Matchers.is(version)
                );
                return Mockito.mock(S3Object.class);
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
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn(1L).when(meta).getContentLength();
        new DefaultResource(
            client, "i", "", Range.ENTIRE, Version.LATEST,
            Mockito.mock(DomainStatsData.class)
        ).close();
        Mockito.verify(object, Mockito.times(1)).close();
    }

    /**
     * DefaultResource closes the underlying object when obtaining the full
     * object size from the Content-Range header.
     * @throws Exception If there is some problem inside
     */
    @Test
    void closesUnderlyingObjectWhenSizeIsInvoked() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn((long) 10).when(meta).getContentLength();
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
        Mockito.verify(object, Mockito.times(1)).close();
    }

    /**
     * DefaultResource can get Content-Encoding info.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsContentEncodingHeaderFromAmazonObject() throws Exception {
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final ObjectMetadata meta = Mockito.mock(ObjectMetadata.class);
        Mockito.doReturn(meta).when(object).getObjectMetadata();
        Mockito.doReturn("gzip").when(meta).getContentEncoding();
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
