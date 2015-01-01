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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import lombok.experimental.Builder;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

/**
 * Mocker of {@link Resource}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class ResourceMocker {

    /**
     * The mock.
     */
    private final transient MkResource.MkResourceBuilder resource = MkResource
        .builder();

    /**
     * Public ctor.
     */
    public ResourceMocker() {
        this.withContent("no content");
        this.resource.status(HttpURLConnection.HTTP_OK);
    }

    /**
     * With this content.
     * @param content The content
     * @return This object
     */
    public ResourceMocker withContent(final String content) {
        this.resource.content(content.toCharArray());
        return this;
    }

    /**
     * With this headers.
     * @param headers The headers
     * @return This object
     */
    public ResourceMocker withHeaders(final String... headers) {
        this.resource.headers(Arrays.asList(headers));
        return this;
    }

    /**
     * Convert resource to string.
     * @param res The resource
     * @return Its text
     * @throws IOException If fails
     */
    public static String toString(final Resource res) throws IOException {
        return new String(ResourceMocker.toByteArray(res), Charsets.UTF_8);
    }

    /**
     * Convert resource to byte array.
     * @param res The resource
     * @return Its text
     * @throws IOException If fails
     */
    public static byte[] toByteArray(final Resource res) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.writeTo(baos);
        return baos.toByteArray();
    }

    /**
     * Mock it.
     * @return The resource
     */
    public Resource mock() {
        return this.resource.build();
    }

    @Builder
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static class MkResource implements Resource {
        /**
         * The resource content.
         */
        private final transient char[] content;
        /**
         * The resource status.
         */
        private final transient int status;
        /**
         * The resource headers.
         */
        private final transient Collection<String> headers;
        /**
         * The resource etag.
         */
        private final transient String etag;
        /**
         * The resource lastModified.
         * @checkstyle MemberName (3 lines)
         */
        private final transient Date lastModified;
        /**
         * The resource contentType.
         * @checkstyle MemberName (3 lines)
         */
        private final transient String contentType;

        @Override
        public void close() throws IOException {
            // do nothing.
        }

        @Override
        public int status() {
            return this.status;
        }

        @Override
        public long writeTo(final OutputStream output) throws IOException {
            IOUtils.write(this.content, output, Charsets.UTF_8);
            return 0;
        }

        @Override
        public Collection<String> headers() throws IOException {
            return this.headers;
        }

        @Override
        public String etag() {
            return this.etag;
        }

        @Override
        public Date lastModified() {
            return this.lastModified;
        }

        @Override
        public String contentType() {
            return this.contentType;
        }
    }
}
