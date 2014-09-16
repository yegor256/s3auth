/**
 * Copyright (c) 2012-2014, s3auth.com
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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Default implementation of {@link Resource}.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@EqualsAndHashCode(of = { "bucket", "key", "range" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class DefaultResource implements Resource {
    /**
     * Amazon S3 client.
     */
    private final transient AmazonS3 client;

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
     * The object retrieved on construction.
     */
    private final transient S3Object object;

    /**
     * Domain Stats.
     */
    private final transient DomainStatsData stats;

    /**
     * Public ctor.
     * @param clnt Amazon S3 client
     * @param bckt Bucket name
     * @param name Key name
     * @param rng Range to deliver
     * @param ver Version of object to retrieve
     * @param dstats Domain stats data
     * @checkstyle ParameterNumber (5 lines)
     */
    DefaultResource(@NotNull final AmazonS3 clnt,
        @NotNull final String bckt, @NotNull final String name,
        @NotNull final Range rng, @NotNull final Version ver,
        @NotNull final DomainStatsData dstats) {
        this.client = clnt;
        this.bucket = bckt;
        this.key = name;
        this.range = rng;
        this.version = ver;
        this.object = this.client.getObject(
            this.request(this.range, this.version)
        );
        this.stats = dstats;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", this.bucket, this.key);
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
        ignore = DefaultResource.StreamingException.class
    )
    public long writeTo(@NotNull final OutputStream output) throws IOException {
        final InputStream input = this.object.getObjectContent();
        assert input != null;
        int total = 0;
        // @checkstyle MagicNumber (1 line)
        final byte[] buffer = new byte[16 * 1024];
        try {
            while (true) {
                final int count;
                try {
                    count = input.read(buffer);
                } catch (final IOException ex) {
                    throw new DefaultResource.StreamingException(
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
                    throw new DefaultResource.StreamingException(
                        String.format(
                            // @checkstyle LineLength (1 line)
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
        } finally {
            input.close();
        }
        return total;
    }

    @Override
    @NotNull
    public Collection<String> headers() {
        final ObjectMetadata meta = this.object.getObjectMetadata();
        final Collection<String> headers = new LinkedList<String>();
        headers.add(
            DefaultResource.header(
                HttpHeaders.CONTENT_LENGTH,
                Long.toString(meta.getContentLength())
            )
        );
        if (meta.getContentType() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.CONTENT_TYPE,
                    meta.getContentType()
                )
            );
        }
        if (meta.getContentEncoding() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.CONTENT_ENCODING,
                    meta.getContentEncoding()
                )
            );
        }
        if (meta.getETag() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.ETAG,
                    meta.getETag()
                )
            );
        }
        headers.add(
            DefaultResource.header(
                HttpHeaders.CACHE_CONTROL,
                StringUtils.defaultString(
                    meta.getCacheControl(),
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
        return this.object.getObjectMetadata().getETag();
    }

    @Override
    public Date lastModified() {
        return new Date(
            this.object.getObjectMetadata().getLastModified().getTime()
        );
    }

    @Override
    public String contentType() {
        return this.object.getObjectMetadata().getContentType();
    }

    @Override
    public void close() throws IOException {
        this.object.close();
    }

    /**
     * Create a HTTP header from name and value.
     * @param name Name of the header
     * @param value The value
     * @return Full HTTP header string
     */
    @NotNull
    private static String header(@NotNull final String name,
        @NotNull final String value) {
        return String.format("%s: %s", name, value);
    }

    /**
     * Make S3 request with a specified range.
     * @param rng Range to request
     * @param ver Version of object to fetch
     * @return Request
     */
    private GetObjectRequest request(final Range rng, final Version ver) {
        final GetObjectRequest request =
            new GetObjectRequest(this.bucket, this.key);
        if (!rng.equals(Range.ENTIRE)) {
            request.withRange(rng.first(), rng.last());
        }
        if (!ver.latest()) {
            request.withVersionId(ver.version());
        }
        return request;
    }

    /**
     * Get total size of an S3 object.
     * @return Size of it in bytes
     */
    private long size() {
        final long size;
        if (this.range.equals(Range.ENTIRE)) {
            size = this.object.getObjectMetadata().getContentLength();
        } else {
            S3Object obj = null;
            try {
                obj = this.client.getObject(
                    this.request(Range.ENTIRE, this.version)
                );
                size = obj.getObjectMetadata().getContentLength();
            } finally {
                IOUtils.closeQuietly(obj);
            }
        }
        return size;
    }

    /**
     * Custom IO exception.
     */
    private static final class StreamingException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA781E111179L;
        /**
         * Public ctor.
         * @param cause The cause of it
         * @param thr The cause of it
         */
        StreamingException(final String cause, final Throwable thr) {
            super(
                String.format("%s: '%s'", cause, thr.getMessage()),
                thr
            );
        }
    }

}
