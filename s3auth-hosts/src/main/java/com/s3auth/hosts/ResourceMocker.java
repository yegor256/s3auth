/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Mocker of {@link Resource}.
 * @since 0.0.1
 */
public final class ResourceMocker {

    /**
     * The mock.
     */
    private final transient MkResourceBuilder resource =
        new MkResourceBuilder();

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
}
