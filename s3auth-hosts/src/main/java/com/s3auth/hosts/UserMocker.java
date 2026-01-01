/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.urn.URN;
import java.net.URI;
import java.util.Random;
import lombok.Builder;

/**
 * Mocker of {@link User}.
 *
 * @since 0.0.1
 */
public final class UserMocker {

    /**
     * The mock.
     */
    private final transient MkUser.MkUserBuilder user = MkUser.builder();

    /**
     * Random generator.
     */
    private final transient Random rand = new Random();

    /**
     * Default one.
     * @return This object
     */
    public UserMocker init() {
        this.withIdentity(
            new URN(
                "facebook",
                Integer.toString(Math.abs(this.rand.nextInt(Integer.MAX_VALUE)))
            )
        );
        this.user.name("John Doe");
        this.user.photo(URI.create("#"));
        return this;
    }

    /**
     * With provided identity.
     * @param identity The identity
     * @return This object
     */
    public UserMocker withIdentity(final URN identity) {
        this.user.identity(identity);
        return this;
    }

    /**
     * With provided identity.
     * @param identity The identity
     * @return This object
     */
    public UserMocker withIdentity(final String identity) {
        return this.withIdentity(URN.create(identity));
    }

    /**
     * Mock it.
     * @return The user
     */
    public User mock() {
        return this.user.build();
    }

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @Builder
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static class MkUser implements User {
        /**
         * The User identity.
         */
        private final transient URN identity;

        /**
         * The user name.
         */
        private final transient String name;

        /**
         * The User photo.
         */
        private final transient URI photo;

        @Override
        public URN identity() {
            return this.identity;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public URI photo() {
            return this.photo;
        }
    }
}
