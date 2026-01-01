/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.Array;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;

/**
 * A {@link Host} that temporarily rejects certain resources, by regex.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.0.1
 */
@Immutable
@EqualsAndHashCode(of = "host")
final class RejectingHost implements Host {

    /**
     * The original host.
     */
    private final transient Host host;

    /**
     * Regexs to reject.
     */
    private final transient Array<String> patterns;

    /**
     * Public ctor.
     * @param hst Original host
     * @param ptns Patterns
     */
    RejectingHost(final Host hst, final String... ptns) {
        this.host = hst;
        this.patterns = new Array<>(ptns);
    }

    @Override
    public String toString() {
        return this.host.toString();
    }

    @Override
    public void close() throws IOException {
        this.host.close();
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
    public Resource fetch(final URI uri, final Range range,
        final Version version) throws IOException {
        final String path = uri.toString();
        boolean reject = false;
        for (final String ptn : this.patterns) {
            if (path.matches(ptn)) {
                reject = true;
                break;
            }
        }
        final Resource resource;
        if (reject) {
            resource = new Resource.PlainText(
                "your resource it temporary disabled, sorry"
            );
        } else {
            resource = this.host.fetch(uri, range, version);
        }
        return resource;
    }

    @Override
    public boolean isHidden(final URI uri) throws IOException {
        return this.host.isHidden(uri);
    }

    @Override
    public boolean authorized(final String user,
        final String password) throws IOException {
        return this.host.authorized(user, password);
    }

}
