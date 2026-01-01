/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import lombok.Builder;
import org.apache.commons.io.IOUtils;

/**
 * Mocker of {@link Resource}.
 *
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
     * Default one.
     * @return This object
     */
    public ResourceMocker init() {
        this.withContent("no content");
        this.resource.status(HttpURLConnection.HTTP_OK);
        return this;
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
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static String toString(final Resource res) throws IOException {
        return new String(ResourceMocker.toByteArray(res), StandardCharsets.UTF_8);
    }

    /**
     * Convert resource to byte array.
     * @param res The resource
     * @return Its text
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
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

    /**
     * Mock.
     *
     * @since 0.0.1
     */
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
        public void close() {
            // do nothing.
        }

        @Override
        public int status() {
            return this.status;
        }

        @Override
        public long writeTo(final OutputStream output) throws IOException {
            IOUtils.write(this.content, output, StandardCharsets.UTF_8);
            return 0;
        }

        @Override
        public Collection<String> headers() {
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
