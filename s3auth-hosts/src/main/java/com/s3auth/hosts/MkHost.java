/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.net.URI;

/**
 * Mock.
 * @since 0.0.1
 */
final class MkHost implements Host {

    /**
     * The host resource.
     */
    private final transient Resource resource;

    /**
     * Whether the host authorized.
     */
    private final transient boolean authorized;

    /**
     * Whether the host is hidden.
     */
    private final transient boolean hidden;

    /**
     * The host syslog.
     */
    private final transient String syslog;

    /**
     * Constructor.
     * @param res The resource
     * @param auth The authorized
     * @param hid The hidden
     * @param log The syslog
     */
    MkHost(final Resource res, final boolean auth,
        final boolean hid, final String log) {
        this.resource = res;
        this.authorized = auth;
        this.hidden = hid;
        this.syslog = log;
    }

    @Override
    public void close() {
        // do nothing.
    }

    @Override
    public Resource fetch(final URI uri, final Range range,
        final Version version) {
        return this.resource;
    }

    @Override
    public boolean isHidden(final URI uri) {
        return this.hidden;
    }

    @Override
    public boolean authorized(final String user, final String password) {
        return this.authorized;
    }

    @Override
    public String syslog() {
        return this.syslog;
    }

    @Override
    public Stats stats() {
        return null;
    }
}
