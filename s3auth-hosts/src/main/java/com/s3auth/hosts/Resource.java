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

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.io.IOUtils;

/**
 * Found resource.
 *
 * <p>Implementation must be immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public interface Resource {

    /**
     * Simple resource made out of plain text.
     */
    class PlainText implements Resource {
        /**
         * Plain text to show.
         */
        private final transient String text;
        /**
         * Public ctor.
         * @param txt The text to show
         */
        public PlainText(final String txt) {
            this.text = txt;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.format("text-%d-chars", this.text.length());
        }
        /**
         * {@inheritDoc}
         */
        @Override
        @Loggable(Loggable.DEBUG)
        public long writeTo(final OutputStream stream) throws IOException {
            IOUtils.write(this.text, stream);
            return this.text.getBytes().length;
        }
        /**
         * {@inheritDoc}
         */
        @Override
        public Collection<String> headers() {
            return Arrays.asList(
                String.format(
                    "%s: text/plain",
                    HttpHeaders.CONTENT_TYPE
                ),
                String.format(
                    "%s: %d",
                    HttpHeaders.CONTENT_LENGTH,
                    this.text.getBytes().length
                )
            );
        }
    }

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

}
