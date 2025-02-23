/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Timeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * A {@link Host} that does everything fast.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.2
 */
@Immutable
@EqualsAndHashCode(of = "origin")
final class FastHost implements Host {

    /**
     * The original host.
     */
    private final transient Host origin;

    /**
     * Public ctor.
     * @param hst Original host
     */
    FastHost(final Host hst) {
        this.origin = hst;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }

    @Override
    public String syslog() {
        return this.origin.syslog();
    }

    @Override
    public Stats stats() {
        return this.origin.stats();
    }

    @Override
    @Timeable(limit = 2, unit = TimeUnit.MINUTES)
    public Resource fetch(final URI uri, final Range range,
        final Version version) throws IOException {
        return this.origin.fetch(uri, range, version);
    }

    @Override
    public boolean isHidden(final URI uri) throws IOException {
        return this.origin.isHidden(uri);
    }

    @Override
    public boolean authorized(final String user,
        final String password) throws IOException {
        return this.origin.authorized(user, password);
    }

}
