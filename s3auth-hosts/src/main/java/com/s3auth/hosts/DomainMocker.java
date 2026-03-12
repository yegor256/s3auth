/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

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
    private final transient MkDomainBuilder domain = new MkDomainBuilder();

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
     * Builder for MkDomain.
     *
     * @since 0.0.1
     */
    @SuppressWarnings("PMD.TooManyFields")
    private static final class MkDomainBuilder {
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

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static final class MkDomain implements Domain {
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
         * @checkstyle ParameterNumberCheck (5 lines)
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
}
