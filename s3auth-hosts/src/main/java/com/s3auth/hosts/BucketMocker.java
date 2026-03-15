/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import software.amazon.awssdk.services.s3.S3Client;

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
    private final transient MkBucketBuilder bucket = new MkBucketBuilder();

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
        this.withClient(new FakeAws());
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
    public BucketMocker withClient(final S3Client client) {
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
     * Builder for MkBucket.
     *
     * @since 0.0.1
     */
    @SuppressWarnings("PMD.TooManyFields")
    private static final class MkBucketBuilder {
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

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static final class MkBucket implements Bucket {
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
         * The Bucket client.
         */
        private final transient S3Client client;

        /**
         * Constructor.
         * @param nme The name
         * @param keyy The key
         * @param scr The secret
         * @param bkt The bucket
         * @param rgn The region
         * @param clt The client
         * @checkstyle ParameterNumberCheck (5 lines)
         */
        MkBucket(final String nme, final String keyy, final String scr,
            final String bkt, final String rgn, final S3Client clt) {
            this.name = nme;
            this.key = keyy;
            this.secret = scr;
            this.bucket = bkt;
            this.region = rgn;
            this.client = clt;
        }

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
            return "";
        }

        @Override
        public S3Client client() {
            return this.client;
        }
    }
}
