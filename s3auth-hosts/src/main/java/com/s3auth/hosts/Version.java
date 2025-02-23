/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * S3 Object version.
 * @since 0.0.1
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface Version {

    /**
     * Specify that the latest version be fetched.
     *
     * @since 0.0.1
     */
    Version LATEST = new Version() {
        @Override
        public boolean latest() {
            return true;
        }

        @Override
        public boolean list() {
            return false;
        }

        @Override
        public String version() {
            throw new UnsupportedOperationException("Version is unspecified.");
        }
    };

    /**
     * Specify that the object's versions be listed.
     *
     * @since 0.0.1
     */
    Version LIST = new Version() {
        @Override
        public boolean latest() {
            return false;
        }

        @Override
        public boolean list() {
            return true;
        }

        @Override
        public String version() {
            throw new UnsupportedOperationException("Version is unspecified.");
        }
    };

    /**
     * Flag specifying whether the latest version is to be fetched.
     * @return Boolean value true, if we're fetching the latest version
     */
    boolean latest();

    /**
     * Flag specifying whether versions should be listed instead of obtaining
     * a particular version.
     * @return Boolean value true, if we're fetching the list of versions
     */
    boolean list();

    /**
     * Version ID of the S3 object.
     * @return Version ID
     */
    String version();

    /**
     * Simple implementation.
     *
     * @since 0.0.1
     */
    @Immutable
    @Loggable(Loggable.DEBUG)
    @ToString
    @EqualsAndHashCode(of = "ver")
    final class Simple implements Version {
        /**
         * The version ID.
         */
        private final String ver;

        /**
         * Public ctor.
         * @param version Version ID string
         */
        public Simple(final String version) {
            this.ver = version;
        }

        @Override
        public boolean latest() {
            return false;
        }

        @Override
        public boolean list() {
            return false;
        }

        @Override
        public String version() {
            return this.ver;
        }
    }

}
