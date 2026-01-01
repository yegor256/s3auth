/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.util.Collections;
import java.util.Set;

/**
 * Mocker of {@link Hosts}.
 *
 * @since 0.0.1
 */
public final class HostsMocker {

    /**
     * The mock.
     */
    private final transient Hosts hosts = new MkHosts();

    /**
     * Mock it.
     * @return The hosts
     */
    public Hosts mock() {
        return this.hosts;
    }

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    private static class MkHosts implements Hosts {
        @Override
        public void close() {
            // do nothing.
        }

        @Override
        public Host find(final String domain) {
            return new HostMocker().init().mock();
        }

        @Override
        public Set<Domain> domains(final User user) {
            return Collections.emptySet();
        }
    }
}
