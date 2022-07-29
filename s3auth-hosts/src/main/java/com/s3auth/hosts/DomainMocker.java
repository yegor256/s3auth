/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the s3auth.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.s3auth.hosts;

import lombok.experimental.Builder;

/**
 * Mocker of {@link Domain}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
     * Public ctor.
     */
    public DomainMocker() {
        this.withName("localhost");
        this.withRegion("s3");
        this.withBucket("bucket");
        this.withKey("AAAAAAAAAAAAAAAAAAAA");
        this.withSecret("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        this.withSyslog("localhost:514");
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
