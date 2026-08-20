/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

/**
 * Builder for MkDomain.
 * @since 0.0.1
 */
final class MkDomainBuilder {

    /**
     * The Domain Name.
     */
    private String name;

    /**
     * The Domain key.
     */
    private String key;

    /**
     * The Domain secret.
     */
    private String secret;

    /**
     * The Domain bucket.
     */
    private String bucket;

    /**
     * The Domain region.
     */
    private String region;

    /**
     * The Domain syslog.
     */
    private String syslog;

    /**
     * Set name.
     * @param value The name
     * @return This builder
     */
    MkDomainBuilder name(final String value) {
        this.name = value;
        return this;
    }

    /**
     * Set key.
     * @param value The key
     * @return This builder
     */
    MkDomainBuilder key(final String value) {
        this.key = value;
        return this;
    }

    /**
     * Set secret.
     * @param value The secret
     * @return This builder
     */
    MkDomainBuilder secret(final String value) {
        this.secret = value;
        return this;
    }

    /**
     * Set bucket.
     * @param value The bucket
     * @return This builder
     */
    MkDomainBuilder bucket(final String value) {
        this.bucket = value;
        return this;
    }

    /**
     * Set region.
     * @param value The region
     * @return This builder
     */
    MkDomainBuilder region(final String value) {
        this.region = value;
        return this;
    }

    /**
     * Set syslog.
     * @param value The syslog
     * @return This builder
     */
    MkDomainBuilder syslog(final String value) {
        this.syslog = value;
        return this;
    }

    /**
     * Build the domain.
     * @return The domain
     */
    MkDomain build() {
        return new MkDomain(
            this.name, this.key, this.secret,
            this.bucket, this.region, this.syslog
        );
    }
}
