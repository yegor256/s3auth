/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.util.Objects;
import java.util.Set;

/**
 * Decorator of {@link Hosts}, adds syslog capabilities for each domain.
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @since 0.0.1
 */
@Immutable
@Loggable(Loggable.DEBUG)
public final class SyslogHosts implements Hosts {

    /**
     * The underlying Hosts instance.
     */
    private final transient Hosts hosts;

    /**
     * Public ctor.
     * @param hsts The hosts to add syslog capability to
     */
    public SyslogHosts(final Hosts hsts) {
        this.hosts = hsts;
    }

    @Override
    public String toString() {
        return String.format("SyslogHosts(%s)", this.hosts);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.hosts);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SyslogHosts
            && Objects.equals(this.hosts, ((SyslogHosts) obj).hosts);
    }

    @Override
    public void close() throws IOException {
        this.hosts.close();
    }

    @Override
    public Host find(final String domain) throws IOException {
        return new SyslogHost(this.hosts.find(domain));
    }

    @Override
    public Set<Domain> domains(final User user) throws IOException {
        return this.hosts.domains(user);
    }
}
