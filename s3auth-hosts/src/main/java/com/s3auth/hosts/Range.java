/*
 * Copyright (c) 2012-2025, Yegor Bugayenko
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
    @ToString
    @EqualsAndHashCode(of = { "frst", "lst" })
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
    }

}
