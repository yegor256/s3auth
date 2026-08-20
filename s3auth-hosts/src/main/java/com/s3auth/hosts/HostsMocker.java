/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

/**
 * Mocker of {@link Hosts}.
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
}
