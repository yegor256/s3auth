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

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Default implementation of {@link Resource}.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode(of = "object")
@Loggable(Loggable.DEBUG)
final class DefaultResource implements Resource {

    /**
     * The object to work with.
     */
    private final transient S3Object object;

    /**
     * The range.
     */
    private final transient Range range;

    /**
     * Total size of the S3 object.
     */
    private final transient long size;

    /**
     * Public ctor.
     * @param obj S3 object
     */
    public DefaultResource(@NotNull final S3Object obj) {
        this(obj, Range.ENTIRE, 0);
    }

    /**
     * Public ctor.
     * @param obj S3 object
     * @param rng Range served
     * @param bytes Total size in bytes
     */
    public DefaultResource(@NotNull final S3Object obj,
        @NotNull final Range rng, final long bytes) {
        this.object = obj;
        this.range = rng;
        this.size = bytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int status() {
        int status;
        if (this.range.equals(Range.ENTIRE)) {
            status = HttpURLConnection.HTTP_OK;
        } else {
            status = HttpURLConnection.HTTP_PARTIAL;
        }
        return status;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long writeTo(@NotNull final OutputStream output) throws IOException {
        final InputStream input = this.object.getObjectContent();
        assert input != null;
        long total = 0;
        // @checkstyle MagicNumber (1 line)
        final byte[] buffer = new byte[16 * 1024];
        try {
            while (true) {
                int count = 0;
                try {
                    count = input.read(buffer);
                } catch (IOException ex) {
                    throw new DefaultResource.StreamingException(
                        String.format(
                            "failed to read %s/%s, range=%s, size=%d, total=%d",
                            this.object.getBucketName(),
                            this.object.getKey(),
                            this.range,
                            this.size,
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
                } catch (IOException ex) {
                    throw new DefaultResource.StreamingException(
                        String.format(
                            // @checkstyle LineLength (1 line)
                            "failed to write %s/%s, range=%s, size=%d, total=%d, count=%d",
                            this.object.getBucketName(),
                            this.object.getKey(),
                            this.range,
                            this.size,
                            total,
                            count
                        ),
                        ex
                    );
                }
                total += count;
            }
        } finally {
            input.close();
        }
        return total;
    }

    /**
     * {@inheritDoc}
     */
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
        if (meta.getETag() != null) {
            headers.add(
                DefaultResource.header(
                    HttpHeaders.ETAG,
                    meta.getETag()
                )
            );
        }
        headers.add(DefaultResource.header("Accept-Ranges", "bytes"));
        if (!this.range.equals(Range.ENTIRE) && this.size > 0) {
            headers.add(
                DefaultResource.header(
                    "Content-Range",
                    String.format(
                        "bytes %d-%d/%d",
                        this.range.first(),
                        this.range.last(),
                        this.size
                    )
                )
            );
        }
        return headers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String etag() {
        return this.object.getObjectMetadata().getETag();
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
        public StreamingException(final String cause, final Throwable thr) {
            super(
                String.format("%s: '%s'", cause, thr.getMessage()),
                thr
            );
        }
    }

}
