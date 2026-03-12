/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.net.URI;

/**
 * Mocker of {@link Host}.
 *
 * @since 0.0.1
 */
public final class HostMocker {

    /**
     * The mock.
     */
    private final transient MkHostBuilder host = new MkHostBuilder();

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
     * Builder for MkHost.
     *
     * @since 0.0.1
     */
    private static final class MkHostBuilder {
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

    /**
     * Mock.
     *
     * @since 0.0.1
     */
    @SuppressWarnings({ "PMD.TooManyMethods",
        "PMD.AvoidFieldNameMatchingMethodName" })
    private static final class MkHost implements Host {
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
         * @checkstyle ParameterNumberCheck (5 lines)
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
}
