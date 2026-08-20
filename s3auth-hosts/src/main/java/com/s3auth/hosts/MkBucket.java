/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import software.amazon.awssdk.services.s3.S3Client;

/**
 * Mock.
 * @since 0.0.1
 */
@SuppressWarnings("PMD.DataClass")
final class MkBucket implements Bucket {

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
