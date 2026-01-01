/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;

/**
 * Found resource.
 *
 * @since 0.0.1
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
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
     * @return The last modified date.
     */
    Date lastModified();

    /**
     * Get the resource's HTTP Content-Type.
     * @return The HTTP Content-Type of the resource.
     */
    String contentType();

    /**
     * Simple resource made out of plain text.
     *
     * @since 0.0.1
     */
    @Immutable
    @ToString
    @EqualsAndHashCode(of = "text")
    @Loggable(Loggable.DEBUG)
    final class PlainText implements Resource {
        /**
         * Plain text to show.
         */
        @Immutable.Array
        private final transient byte[] text;

        /**
         * Last modified date to return. Equal to the time of object creation.
         */
        private final transient long modified;

        /**
         * Headers associated with this resource.
         */
        private final Array<String> hdrs;

        /**
         * Public ctor.
         * @param txt The text to show
         */
        public PlainText(@NotNull final String txt) {
            this.modified = System.currentTimeMillis();
            this.text = txt.getBytes(StandardCharsets.UTF_8);
            this.hdrs = new Array<>(
                String.format(
                    "%s: %s",
                    HttpHeaders.CONTENT_TYPE,
                    this.contentType()
                ),
                String.format(
                    "%s: %d",
                    HttpHeaders.CONTENT_LENGTH,
                    this.text.length
                )
            );
        }

        @Override
        public int status() {
            return HttpURLConnection.HTTP_OK;
        }

        @Override
        public long writeTo(@NotNull final OutputStream stream)
            throws IOException {
            IOUtils.write(this.text, stream);
            return this.text.length;
        }

        @Override
        public String etag() {
            return DigestUtils.md5Hex(this.text);
        }

        @Override
        public Date lastModified() {
            return new Date(this.modified);
        }

        @Override
        public String contentType() {
            return "text/plain";
        }

        @Override
        @NotNull
        public Collection<String> headers() {
            return this.hdrs;
        }

        @Override
        public void close() {
            // nothing to do
        }
    }

}
