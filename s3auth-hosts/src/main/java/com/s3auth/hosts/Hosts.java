/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * Collection of hosts.
 *
 * <p>Implementation must be thread-safe.
 *
 * @since 0.0.1
 */
@Immutable
public interface Hosts extends Closeable {

    /**
     * Find one host by domain name.
     * @param domain The domain name
     * @return Host found
     * @throws IOException If not found or some other IO problem
     */
    Host find(String domain) throws IOException;

    /**
     * Get domains of the given user.
     * @param user The user
     * @return Modifiable collection of domains
     * @throws IOException If some error inside
     */
    Set<Domain> domains(User user) throws IOException;

    /**
     * Thrown by {@link #find(String)} if domain is not found.
     *
     * @since 0.0.1
     */
    class NotFoundException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA789ED21479L;

        /**
         * Public ctor.
         * @param cause The cause of it
         */
        public NotFoundException(final String cause) {
            super(cause);
        }
    }

}
