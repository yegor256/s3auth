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

import com.amazonaws.services.s3.AmazonS3;
import lombok.Builder;

/**
 * Mocker of {@link Bucket}.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
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
     * Public ctor.
     */
    public BucketMocker() {
        this.withName("maven.s3auth.com");
        this.withBucket("s3auth");
        this.withRegion("s3-ap-southeast-1");
        this.withKey("AAAAAAAAAAAAAAAAAAAA");
        this.withSecret("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        this.withClient(new MkAmazonS3());
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
