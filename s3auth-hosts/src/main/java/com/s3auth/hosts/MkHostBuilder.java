/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

/**
 * Builder for MkHost.
 * @since 0.0.1
 */
final class MkHostBuilder {

    /**
     * The host resource.
     */
    private Resource resource;

    /**
     * Whether the host authorized.
     */
    private boolean authorized;

    /**
     * Whether the host is hidden.
     */
    private boolean hidden;

    /**
     * The host syslog.
     */
    private String syslog;

    /**
     * Set resource.
     * @param value The resource
     * @return This builder
     */
    MkHostBuilder resource(final Resource value) {
        this.resource = value;
        return this;
    }

    /**
     * Set authorized.
     * @param value The authorized
     * @return This builder
     */
    MkHostBuilder authorized(final boolean value) {
        this.authorized = value;
        return this;
    }

    /**
     * Set hidden.
     * @param value The hidden
     * @return This builder
     */
    MkHostBuilder hidden(final boolean value) {
        this.hidden = value;
        return this;
    }

    /**
     * Set syslog.
     * @param value The syslog
     * @return This builder
     */
    MkHostBuilder syslog(final String value) {
        this.syslog = value;
        return this;
    }

    /**
     * Build the host.
     * @return The host
     */
    MkHost build() {
        return new MkHost(
            this.resource, this.authorized, this.hidden, this.syslog
        );
    }
}
