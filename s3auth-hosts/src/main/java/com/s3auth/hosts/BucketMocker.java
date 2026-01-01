/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.s3.AmazonS3;
import lombok.Builder;

/**
 * Mocker of {@link Bucket}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class BucketMocker {

    /**
     * The mock.
     */
    private final transient MkBucket.MkBucketBuilder bucket = MkBucket
        .builder();

    /**
     * Init.
     * @return This object
     */
    public BucketMocker init() {
        this.withName("maven.s3auth.com");
        this.withBucket("s3auth");
        this.withRegion("ap-southeast-1");
        this.withKey("AAAAAAAAAAAAAAAAAAAA");
        this.withSecret("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        this.withClient(new MkAmazonS3());
        return this;
    }

    /**
     * With this name.
     * @param name The name
     * @return This object
     */
    public BucketMocker withName(final String name) {
        this.bucket.name(name);
        return this;
    }

    /**
     * With this key.
     * @param key The key
     * @return This object
     */
    public BucketMocker withKey(final String key) {
        this.bucket.key(key);
        return this;
    }

    /**
     * With this secret.
     * @param secret The secret
     * @return This object
     */
    public BucketMocker withSecret(final String secret) {
        this.bucket.secret(secret);
        return this;
    }

    /**
     * With this bucket.
     * @param bckt The bucket
     * @return This object
     */
    public BucketMocker withBucket(final String bckt) {
        this.bucket.bucket(bckt);
        return this;
    }

    /**
     * With this region.
     * @param region The region
     * @return This object
     */
    public BucketMocker withRegion(final String region) {
        this.bucket.region(region);
        return this;
    }

    /**
     * With this client.
     * @param client The client
     * @return This object
     */
    public BucketMocker withClient(final AmazonS3 client) {
        this.bucket.client(client);
        return this;
    }

    /**
     * Mock it.
     * @return The bucket
     */
    public Bucket mock() {
        return this.bucket.build();
    }

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @Builder
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static class MkBucket implements Bucket {
        /**
         * The Bucket Name.
         */
        private final transient String name;

        /**
         * The Bucket key.
         */
        private final transient String key;

        /**
         * The Bucket secret.
         */
        private final transient String secret;

        /**
         * The Bucket bucket.
         */
        private final transient String bucket;

        /**
         * The Bucket region.
         */
        private final transient String region;

        /**
         * The Bucket syslog.
         */
        private final transient String syslog;

        /**
         * The Bucket client.
         */
        private final transient AmazonS3 client;

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public String key() {
            return this.key;
        }

        @Override
        public String secret() {
            return this.secret;
        }

        @Override
        public String bucket() {
            return this.bucket;
        }

        @Override
        public String region() {
            return this.region;
        }

        @Override
        public String syslog() {
            return this.syslog;
        }

        @Override
        public AmazonS3 client() {
            return this.client;
        }
    }
}
