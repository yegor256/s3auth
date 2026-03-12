/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;

/**
 * Range of data.
 *
 * @since 0.0.1
 */
@Immutable
public interface Range {

    /**
     * All inclusive range.
     *
     * @since 0.0.1
     */
    Range ENTIRE = new Range() {
        @Override
        public String toString() {
            return "ENTIRE";
        }

        @Override
        public long first() {
            return 0;
        }

        @Override
        public long last() {
            return Long.MAX_VALUE;
        }
    };

    /**
     * First byte to fetch, inclusively.
     * @return Number of byte
     */
    long first();

    /**
     * Last byte to fetch, inclusively.
     * @return Number of byte
     */
    long last();

    /**
     * Simple implementation.
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    final class Simple implements Range {
        /**
         * First byte.
         */
        private final transient long frst;

        /**
         * Last byte.
         */
        private final transient long lst;

        /**
         * Public ctor.
         * @param first First byte
         * @param last Last byte
         */
        public Simple(final long first, final long last) {
            this.frst = first;
            this.lst = last;
        }

        @Override
        public long first() {
            return this.frst;
        }

        @Override
        public long last() {
            return this.lst;
        }

        @Override
        public String toString() {
            return String.format("Range.Simple(%d, %d)", this.frst, this.lst);
        }

        @Override
        public int hashCode() {
            return Long.hashCode(this.frst) * 31 + Long.hashCode(this.lst);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean result;
            if (obj instanceof Simple) {
                final Simple other = (Simple) obj;
                result = this.frst == other.frst && this.lst == other.lst;
            } else {
                result = false;
            }
            return result;
        }
    }

}
