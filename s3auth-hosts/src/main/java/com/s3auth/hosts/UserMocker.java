/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.urn.URN;
import java.net.URI;
import java.util.Random;

/**
 * Mocker of {@link User}.
 *
 * @since 0.0.1
 */
public final class UserMocker {

    /**
     * The mock.
     */
    private final transient MkUserBuilder user = new MkUserBuilder();

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
     * Builder for MkUser.
     *
     * @since 0.0.1
     */
    private static final class MkUserBuilder {
        /**
         * The User identity.
         */
        private URN identity;

        /**
         * The user name.
         */
        private String name;

        /**
         * The User photo.
         */
        private URI photo;

        /**
         * Set identity.
         * @param value The identity
         * @return This builder
         */
        MkUserBuilder identity(final URN value) {
            this.identity = value;
            return this;
        }

        /**
         * Set name.
         * @param value The name
         * @return This builder
         */
        MkUserBuilder name(final String value) {
            this.name = value;
            return this;
        }

        /**
         * Set photo.
         * @param value The photo
         * @return This builder
         */
        MkUserBuilder photo(final URI value) {
            this.photo = value;
            return this;
        }

        /**
         * Build the user.
         * @return The user
         */
        MkUser build() {
            return new MkUser(this.identity, this.name, this.photo);
        }
    }

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static final class MkUser implements User {
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

        /**
         * Constructor.
         * @param ident The identity
         * @param usr The name
         * @param pht The photo
         */
        MkUser(final URN ident, final String usr, final URI pht) {
            this.identity = ident;
            this.name = usr;
            this.photo = pht;
        }

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
