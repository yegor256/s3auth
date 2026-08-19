/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

/**
 * Found resource.
 * @since 0.0.1
 */
@Immutable
public interface Resource extends Closeable {

    /**
     * Get HTTP status.
     * @return The status
     */
    int status();

    /**
     * Write its content to the writer.
     * @param stream The stream to write to
     * @return How many bytes were written
     * @throws IOException If some error with I/O inside
     */
    long writeTo(OutputStream stream) throws IOException;

    /**
     * Get a collection of all necessary HTTP headers for this resource.
     * @return Collection of HTTP headers
     * @throws IOException If some error with I/O inside
     */
    Collection<String> headers() throws IOException;

    /**
     * Get its ETag.
     * @return The etag
     * @link <a href="https://en.wikipedia.org/wiki/HTTP_ETag">ETag</a>
     */
    String etag();

    /**
     * Get its last modified date.
     * @return The last modified date
     */
    Date lastModified();

    /**
     * Get the resource's HTTP Content-Type.
     * @return The HTTP Content-Type of the resource
     */
    String contentType();

    /**
     * Simple resource made out of plain text.
     * @since 0.0.1
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    final class PlainText implements Resource {

        /**
         * Plain text to show.
         */
        private final transient String raw;

        /**
         * Public ctor.
         * @param txt The text to show
         */
        public PlainText(@NotNull final String txt) {
            this.raw = txt;
        }

        @Override
        public int status() {
            return HttpURLConnection.HTTP_OK;
        }

        @Override
        public long writeTo(@NotNull final OutputStream stream)
            throws IOException {
            IOUtils.write(this.text(), stream);
            return this.text().length;
        }

        @Override
        public String etag() {
            return DigestUtils.md5Hex(this.text());
        }

        @Override
        public Date lastModified() {
            return Date.from(Instant.now());
        }

        @Override
        public String contentType() {
            return "text/plain";
        }

        @Override
        @NotNull
        public Collection<String> headers() {
            return this.hdrs();
        }

        @Override
        public void close() {
            // nothing to do
        }

        @Override
        public String toString() {
            return String.format("PlainText(%d chars)", this.raw.length());
        }

        @Override
        public int hashCode() {
            return this.raw.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof PlainText
                && this.raw.equals(((PlainText) obj).raw);
        }

        /**
         * Text as bytes.
         * @return Bytes of the text
         */
        @Cacheable(forever = true)
        private byte[] text() {
            return this.raw.getBytes(StandardCharsets.UTF_8);
        }

        /**
         * HTTP headers for this resource.
         * @return Headers
         */
        @Cacheable(forever = true)
        private Array<String> hdrs() {
            return new Array<>(
                String.format(
                    "%s: %s",
                    HttpHeaders.CONTENT_TYPE,
                    this.contentType()
                ),
                String.format(
                    "%s: %d",
                    HttpHeaders.CONTENT_LENGTH,
                    this.text().length
                )
            );
        }
    }
}
