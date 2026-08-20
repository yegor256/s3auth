/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * Builder for MkBucket.
 * @since 0.0.1
 */
final class MkBucketBuilder {

    /**
     * The Bucket Name.
     */
    private String name;

    /**
     * The Bucket key.
     */
    private String key;

    /**
     * The Bucket secret.
     */
    private String secret;

    /**
     * The Bucket bucket.
     */
    private String bucket;

    /**
     * The Bucket region.
     */
    private String region;

    /**
     * The Bucket client.
     */
    private S3Client client;

    /**
     * Set name.
     * @param value The name
     * @return This builder
     */
    MkBucketBuilder name(final String value) {
        this.name = value;
        return this;
    }

    /**
     * Set key.
     * @param value The key
     * @return This builder
     */
    MkBucketBuilder key(final String value) {
        this.key = value;
        return this;
    }

    /**
     * Set secret.
     * @param value The secret
     * @return This builder
     */
    MkBucketBuilder secret(final String value) {
        this.secret = value;
        return this;
    }

    /**
     * Set bucket.
     * @param value The bucket
     * @return This builder
     */
    MkBucketBuilder bucket(final String value) {
        this.bucket = value;
        return this;
    }

    /**
     * Set region.
     * @param value The region
     * @return This builder
     */
    MkBucketBuilder region(final String value) {
        this.region = value;
        return this;
    }

    /**
     * Set client.
     * @param value The client
     * @return This builder
     */
    MkBucketBuilder client(final S3Client value) {
        this.client = value;
        return this;
    }

    /**
     * Build the bucket.
     * @return The bucket
     */
    MkBucket build() {
        return new MkBucket(
            this.name, this.key, this.secret,
            this.bucket, this.region, this.client
        );
    }
}
