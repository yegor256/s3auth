/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import lombok.Builder;

/**
 * Mocker of {@link Domain}.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class DomainMocker {

    /**
     * The mock.
     */
    private final transient MkDomain.MkDomainBuilder domain = MkDomain
        .builder();

    /**
     * Init.
     * @return This object
     */
    public DomainMocker init() {
        this.withName("localhost");
        this.withRegion("us-east-1");
        this.withBucket("bucket");
        this.withKey("AAAAAAAAAAAAAAAAAAAA");
        this.withSecret("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        this.withSyslog("localhost:514");
        return this;
    }

    /**
     * With this name.
     * @param name The name
     * @return This object
     */
    public DomainMocker withName(final String name) {
        this.domain.name(name);
        return this;
    }

    /**
     * With this key.
     * @param key The key
     * @return This object
     */
    public DomainMocker withKey(final String key) {
        this.domain.key(key);
        return this;
    }

    /**
     * With this secret.
     * @param secret The secret
     * @return This object
     */
    public DomainMocker withSecret(final String secret) {
        this.domain.secret(secret);
        return this;
    }

    /**
     * With this bucket.
     * @param bckt The bucket
     * @return This object
     */
    public DomainMocker withBucket(final String bckt) {
        this.domain.bucket(bckt);
        return this;
    }

    /**
     * With this region.
     * @param region The region
     * @return This object
     */
    public DomainMocker withRegion(final String region) {
        this.domain.region(region);
        return this;
    }

    /**
     * With this syslog.
     * @param syslog The syslog
     * @return This object
     */
    public DomainMocker withSyslog(final String syslog) {
        this.domain.syslog(syslog);
        return this;
    }

    /**
     * Mock it.
     * @return The domain
     */
    public Domain mock() {
        return this.domain.build();
    }

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @Builder
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static class MkDomain implements Domain {
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
}
