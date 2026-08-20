/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.net.URI;

/**
 * Mocker of {@link Host}.
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
}
