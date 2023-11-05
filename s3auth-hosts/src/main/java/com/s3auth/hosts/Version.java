/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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
