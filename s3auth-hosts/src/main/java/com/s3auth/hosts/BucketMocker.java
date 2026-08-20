/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * Mocker of {@link Bucket}.
 * @since 0.0.1
 */
public final class BucketMocker {

    /**
     * The mock.
     */
    private final transient MkBucketBuilder bucket =
        new MkBucketBuilder();

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
}
