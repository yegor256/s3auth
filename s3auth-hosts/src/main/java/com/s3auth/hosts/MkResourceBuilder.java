/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.util.Collection;

/**
 * Builder for MkResource.
 * @since 0.0.1
 */
final class MkResourceBuilder {

    /**
     * The resource content.
     */
    private char[] content;

    /**
     * The resource status.
     */
    private int status;

    /**
     * The resource headers.
     */
    private Collection<String> headers;

    /**
     * Set content.
     * @param value The content
     * @return This builder
     */
    MkResourceBuilder content(final char... value) {
        this.content = value.clone();
        return this;
    }

    /**
     * Set status.
     * @param value The status
     * @return This builder
     */
    MkResourceBuilder status(final int value) {
        this.status = value;
        return this;
    }

    /**
     * Set headers.
     * @param value The headers
     * @return This builder
     */
    MkResourceBuilder headers(final Collection<String> value) {
        this.headers = value;
        return this;
    }

    /**
     * Build the resource.
     * @return The resource
     */
    MkResource build() {
        return new MkResource(this.content, this.status, this.headers);
    }
}
