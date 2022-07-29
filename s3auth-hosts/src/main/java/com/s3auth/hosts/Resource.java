/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.immutable.Array;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;

/**
 * Found resource.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
     * @link <a href="http://en.wikipedia.org/wiki/HTTP_ETag">ETag</a>
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
        private final transient long modified = System.currentTimeMillis();
        /**
         * Headers associated with this resource.
         */
        private final Array<String> hdrs;
        /**
         * Public ctor.
         * @param txt The text to show
         */
        public PlainText(@NotNull final String txt) {
            this.text = txt.getBytes(Charsets.UTF_8);
            this.hdrs = new Array<String>(
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
