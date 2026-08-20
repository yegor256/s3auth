/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Syslog host wrapper.
 * @since 0.0.1
 */
@Immutable
final class SyslogHost implements Host {

    /**
     * Pattern for matching syslog host and port.
     */
    private static final Pattern PATTERN = Pattern.compile(
        "(\\w+)(:(\\d+))?"
    );

    /**
     * The underlying Host instance.
     */
    private final transient Host host;

    /**
     * Constructor.
     * @param hst Host
     */
    SyslogHost(final Host hst) {
        this.host = hst;
    }

    @Override
    public void close() throws IOException {
        this.host.close();
    }

    @Override
    public Resource fetch(final URI uri, final Range range,
        final Version version) throws IOException {
        final Matcher matcher = SyslogHost.PATTERN.matcher(this.host.syslog());
        final Resource res;
        if (matcher.find()) {
            final String syslg = matcher.group(1);
            final int port;
            if (matcher.group(3) == null) {
                port = 514;
            } else {
                port = Integer.parseInt(matcher.group(3));
            }
            res = new SyslogResource(
                this.host.fetch(uri, range, version), uri, syslg, port
            );
        } else {
            res = this.host.fetch(uri, range, version);
        }
        return res;
    }

    @Override
    public boolean isHidden(final URI uri) throws IOException {
        return this.host.isHidden(uri);
    }

    @Override
    public boolean authorized(final String user, final String password)
        throws IOException {
        return this.host.authorized(user, password);
    }

    @Override
    public String syslog() {
        return this.host.syslog();
    }

    @Override
    public Stats stats() {
        return this.host.stats();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.host);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SyslogHost
            && Objects.equals(this.host, ((SyslogHost) obj).host);
    }
}
