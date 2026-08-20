/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.util.Collection;
import software.amazon.awssdk.services.s3.model.S3Object;

/**
 * Objects and common prefixes found while paginating a bucket listing.
 * @since 0.0.1
 */
final class Listing {

    /**
     * S3 objects found.
     */
    private final transient Collection<S3Object> objects;

    /**
     * Common prefixes found.
     */
    private final transient Collection<String> prefixes;

    /**
     * Ctor.
     * @param objs S3 objects found
     * @param prfxs Common prefixes found
     */
    Listing(final Collection<S3Object> objs, final Collection<String> prfxs) {
        this.objects = objs;
        this.prefixes = prfxs;
    }

    /**
     * S3 objects found.
     * @return The objects
     */
    Collection<S3Object> objects() {
        return this.objects;
    }

    /**
     * Common prefixes found.
     * @return The prefixes
     */
    Collection<String> prefixes() {
        return this.prefixes;
    }
}
