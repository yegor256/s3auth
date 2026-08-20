/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * Default implementation of {@link Resource}.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class DefaultResource implements Resource {

    /**
     * Amazon S3 client.
     */
    private final transient S3Client client;

    /**
     * Bucket name.
     */
    private final transient String bucket;

    /**
     * Key in the bucket.
     */
    private final transient String key;

    /**
     * The range.
     */
    private final transient Range range;

    /**
     * The version.
     */
    private final transient Version version;

    /**
     * Domain Stats.
     */
    private final transient DomainStatsData stats;

    /**
     * The object input stream, already opened.
     */
    private final transient ResponseInputStream<GetObjectResponse> stream;

    /**
     * Private ctor, stream opened by {@link #fetch}.
     * @param clnt Amazon S3 client
     * @param bckt Bucket name
     * @param name Key name
     * @param rng Range to deliver
     * @param ver Version of object to retrieve
     * @param dstats Domain stats data
     * @param strm Already opened object stream
     */
    private DefaultResource(final S3Client clnt,
        final String bckt, final String name,
        final Range rng, final Version ver,
        final DomainStatsData dstats,
        final ResponseInputStream<GetObjectResponse> strm) {
        this.client = clnt;
        this.bucket = bckt;
        this.key = name;
        this.range = rng;
        this.version = ver;
        this.stats = dstats;
        this.stream = strm;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", this.bucket, this.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.bucket, this.key, this.range);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean result;
        if (obj instanceof DefaultResource) {
            final DefaultResource other = (DefaultResource) obj;
            result = Objects.equals(this.bucket, other.bucket)
                && Objects.equals(this.key, other.key)
                && Objects.equals(this.range, other.range);
        } else {
            result = false;
        }
        return result;
    }

    @Override
    public int status() {
        final int status;
        if (this.range.equals(Range.ENTIRE)) {
            status = HttpURLConnection.HTTP_OK;
        } else {
            status = HttpURLConnection.HTTP_PARTIAL;
        }
        return status;
    }

    @Override
    @Loggable(
        value = Loggable.DEBUG, limit = Integer.MAX_VALUE,
        ignore = StreamingException.class
    )
    public long writeTo(@NotNull final OutputStream output) throws IOException {
        int total = 0;
        final byte[] buffer = new byte[16 * 1024];
        try (InputStream input = this.stream) {
            while (true) {
                final int count;
                try {
                    count = input.read(buffer);
                } catch (final IOException ex) {
                    throw new StreamingException(
                        String.format(
                            "failed to read %s/%s, range=%s, total=%d",
                            this.bucket,
                            this.key,
                            this.range,
                            total
                        ),
                        ex
                    );
                }
                if (count == -1) {
                    break;
                }
                try {
                    output.write(buffer, 0, count);
                } catch (final IOException ex) {
                    throw new StreamingException(
                        String.format(
                            "failed to write %s/%s, range=%s, total=%d, count=%d",
                            this.bucket,
                            this.key,
                            this.range,
                            total,
                            count
                        ),
                        ex
                    );
                }
                total += count;
            }
            this.stats.put(this.bucket, new Stats.Simple(total));
        }
        return total;
    }

    @Override
    @NotNull
    public Collection<String> headers() {
        final Collection<String> headers = new ArrayList<>(5);
        headers.add(
            DefaultResource.header(
                HttpHeaders.CONTENT_LENGTH,
                Long.toString(this.response().contentLength())
            )
        );
        if (this.response().contentType() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.CONTENT_TYPE,
                    this.response().contentType()
                )
            );
        }
        if (this.response().contentEncoding() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.CONTENT_ENCODING,
                    this.response().contentEncoding()
                )
            );
        }
        if (this.response().eTag() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.ETAG,
                    this.response().eTag()
                )
            );
        }
        headers.add(
            DefaultResource.header(
                HttpHeaders.CACHE_CONTROL,
                StringUtils.defaultIfBlank(
                    this.response().cacheControl(),
                    "must-revalidate"
                )
            )
        );
        headers.add(DefaultResource.header("Accept-Ranges", "bytes"));
        if (!this.range.equals(Range.ENTIRE)) {
            headers.add(
                DefaultResource.header(
                    "Content-Range",
                    String.format(
                        "bytes %d-%d/%d",
                        this.range.first(),
                        this.range.last(),
                        this.size()
                    )
                )
            );
        }
        return headers;
    }

    @Override
    @NotNull
    public String etag() {
        return this.response().eTag();
    }

    @Override
    public Date lastModified() {
        return Date.from(
            Optional.ofNullable(this.response().lastModified())
                .orElseGet(Instant::now)
        );
    }

    @Override
    public String contentType() {
        return this.response().contentType();
    }

    @Override
    public void close() throws IOException {
        this.stream.close();
    }

    /**
     * Fetch an object from S3.
     * @param clnt Amazon S3 client
     * @param loc Coordinates of the object to fetch
     * @param dstats Domain stats data
     * @return Fetched resource
     */
    static DefaultResource fetch(@NotNull final S3Client clnt,
        @NotNull final Locator loc,
        @NotNull final DomainStatsData dstats) {
        return new DefaultResource(
            clnt, loc.bucket(), loc.key(), loc.range(), loc.version(), dstats,
            clnt.getObject(DefaultResource.request(loc))
        );
    }

    @NotNull
    private static String header(@NotNull final String name,
        @NotNull final String value) {
        return String.format("%s: %s", name, value);
    }

    private GetObjectResponse response() {
        return this.stream.response();
    }

    private static GetObjectRequest request(final Locator loc) {
        final GetObjectRequest.Builder builder = GetObjectRequest.builder()
            .bucket(loc.bucket())
            .key(loc.key());
        if (!loc.range().equals(Range.ENTIRE)) {
            builder.range(String.format("bytes=%d-%d", loc.range().first(), loc.range().last()));
        }
        if (!loc.version().latest()) {
            builder.versionId(loc.version().version());
        }
        return builder.build();
    }

    private long size() {
        final long size;
        if (this.range.equals(Range.ENTIRE)) {
            size = this.response().contentLength();
        } else {
            try (
                ResponseInputStream<GetObjectResponse> resp =
                    this.client.getObject(
                        DefaultResource.request(
                            new Locator(
                                this.bucket, this.key, Range.ENTIRE, this.version
                            )
                        )
                    )
            ) {
                size = resp.response().contentLength();
            } catch (final IOException ex) {
                throw new IllegalStateException("Failed to get object size", ex);
            }
        }
        return size;
    }
}
