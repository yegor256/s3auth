/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

/**
 * Mock.
 * @since 0.0.1
 */
@SuppressWarnings("PMD.DataClass")
final class MkDomain implements Domain {

    /**
     * The Domain Name.
     */
    private final transient String name;

    /**
     * The Domain key.
     */
    private final transient String key;

    /**
     * The Domain secret.
     */
    private final transient String secret;

    /**
     * The Domain bucket.
     */
    private final transient String bucket;

    /**
     * The Domain region.
     */
    private final transient String region;

    /**
     * The Domain syslog.
     */
    private final transient String syslog;

    /**
     * Constructor.
     * @param nme The name
     * @param keyy The key
     * @param scr The secret
     * @param bkt The bucket
     * @param rgn The region
     * @param log The syslog
     */
    MkDomain(final String nme, final String keyy, final String scr,
        final String bkt, final String rgn, final String log) {
        this.name = nme;
        this.key = keyy;
        this.secret = scr;
        this.bucket = bkt;
        this.region = rgn;
        this.syslog = log;
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
        return this.syslog;
    }
}
