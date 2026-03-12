/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;

/**
 * Statistics for a given domain.
 * @since 0.0.1
 */
@Immutable
public interface Stats {

    /**
     * The bytes transferred for this domain for the previous week.
     * @return Bytes transferred
     */
    long bytesTransferred();

    /**
     * Simple stats.
     *
     * @since 0.0.1
     */
    @Immutable
    final class Simple implements Stats {
        /**
         * Bytes transferred.
         */
        private final transient long bytes;

        /**
         * Ctor.
         * @param transferred Number of bytes transferred.
         */
        public Simple(final long transferred) {
            this.bytes = transferred;
        }

        @Override
        public long bytesTransferred() {
            return this.bytes;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(this.bytes);
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof Simple
                && this.bytes == ((Simple) obj).bytes;
        }
    }
}
