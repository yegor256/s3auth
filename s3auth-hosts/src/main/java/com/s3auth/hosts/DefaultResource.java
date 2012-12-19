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
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import javax.ws.rs.core.HttpHeaders;

/**
 * Default implementation of {@link Resource}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
final class DefaultResource implements Resource {

    /**
     * The object to work with.
     */
    private final transient S3Object object;

    /**
     * Public ctor.
     * @param obj S3 object
     */
    public DefaultResource(final S3Object obj) {
        this.object = obj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.object.getKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long writeTo(final OutputStream output) throws IOException {
        final InputStream input = this.object.getObjectContent();
        Logger.debug(
            this,
            "#writeTo(): starting to stream %s/%s, redirectLocation=%s",
            this.object.getBucketName(),
            this.object.getKey(),
            this.object.getRedirectLocation()
        );
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
                            "failed to read, total=%d",
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
                            "failed to write, total=%d, count=%d",
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
    public Collection<String> headers() {
        final ObjectMetadata meta = this.object.getObjectMetadata();
        final Collection<String> headers = new LinkedList<String>();
        headers.add(
            DefaultResource.header(
                HttpHeaders.CONTENT_LENGTH,
                Long.toString(meta.getContentLength())
            )
        );
        headers.add(
            DefaultResource.header(
                HttpHeaders.CONTENT_TYPE,
                meta.getContentType()
            )
        );
        return headers;
    }

    /**
     * Create a HTTP header from name and value.
     * @param name Name of the header
     * @param value The value
     * @return Full HTTP header string
     */
    public static String header(final String name, final String value) {
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
