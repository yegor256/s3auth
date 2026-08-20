/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import org.apache.commons.io.IOUtils;

/**
 * Mock.
 * @since 0.0.1
 */
final class MkResource implements Resource {

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
     * Constructor.
     * @param cnt The content
     * @param sts The status
     * @param hdrs The headers
     */
    MkResource(final char[] cnt, final int sts, final Collection<String> hdrs) {
        this.content = cnt.clone();
        this.status = sts;
        this.headers = hdrs;
    }

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
        return "";
    }

    @Override
    public Date lastModified() {
        return null;
    }

    @Override
    public String contentType() {
        return "";
    }
}
