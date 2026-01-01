/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.net.URI;
import lombok.Builder;

/**
 * Mocker of {@link Host}.
 *
 * @since 0.0.1
 */
public final class HostMocker {

    /**
     * The mock.
     */
    private final transient MkHost.MkHostBuilder host = MkHost.builder();

    /**
     * Initialize it.
     * @return This object
     */
    public HostMocker init() {
        this.host
            .resource(new ResourceMocker().init().withContent("hello").mock())
            .authorized(true)
            .hidden(true);
        return this;
    }

    /**
     * With this content for this URI.
     * @param uri The URI to match
     * @param content The content to return
     * @return This object
     */
    public HostMocker withContent(final URI uri, final String content) {
        this.host.resource(new ResourceMocker().init().withContent(content).mock());
        return this;
    }

    /**
     * With this syslog.
     * @param syslog The syslog to return
     * @return This object
     */
    public HostMocker withSyslog(final String syslog) {
        this.host.syslog(syslog);
        return this;
    }

    /**
     * Mock it.
     * @return The host
     */
    public Host mock() {
        return this.host.build();
    }

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @Builder
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static class MkHost implements Host {
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
         * The host stats.
         */
        private final transient Stats stats;

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
            return this.stats;
        }
    }
}
