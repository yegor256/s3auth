/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

/**
 * Coordinates of an S3 object to fetch.
 * @since 0.0.1
 */
@SuppressWarnings("PMD.DataClass")
final class Locator {

    /**
     * Bucket name.
     */
    private final transient String bucket;

    /**
     * Key in the bucket.
     */
    private final transient String key;

    /**
     * The range.
     */
    private final transient Range range;

    /**
     * The version.
     */
    private final transient Version version;

    /**
     * Ctor.
     * @param bckt Bucket name
     * @param name Key name
     * @param rng Range to deliver
     * @param ver Version of object to retrieve
     */
    Locator(final String bckt, final String name,
        final Range rng, final Version ver) {
        this.bucket = bckt;
        this.key = name;
        this.range = rng;
        this.version = ver;
    }

    /**
     * Bucket name.
     * @return The name
     */
    String bucket() {
        return this.bucket;
    }

    /**
     * Key in the bucket.
     * @return The key
     */
    String key() {
        return this.key;
    }

    /**
     * The range.
     * @return The range
     */
    Range range() {
        return this.range;
    }

    /**
     * The version.
     * @return The version
     */
    Version version() {
        return this.version;
    }
}
