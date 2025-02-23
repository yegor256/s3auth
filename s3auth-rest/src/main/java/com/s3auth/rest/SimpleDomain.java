/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import com.s3auth.hosts.Domain;

/**
 * Simple domain.
 *
 * @since 0.1
 */
final class SimpleDomain implements Domain {

    /**
     * Host.
     */
    private final transient String host;

    /**
     * Key.
     */
    private final transient String acc;

    /**
     * Secret.
     */
    private final transient String sec;

    /**
     * Bucket name.
     */
    private final transient String buckt;

    /**
     * Region.
     */
    private final transient String regn;

    /**
     * Syslog host.
     */
    private final transient String slog;

    /**
     * Ctor.
     * @param hst The host name
     */
    SimpleDomain(final String hst) {
        this(hst, "", "", "", "", "");
    }

    /**
     * Constructor.
     * @param hst The host name
     * @param access AWS access key
     * @param scrt AWS secret
     * @param bckt Bucket name
     * @param rgn S3 region
     * @param syslg The syslog host and port
     * @checkstyle ParameterNumber (4 lines)
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    SimpleDomain(final String hst, final String access, final String scrt,
        final String bckt, final String rgn, final String syslg) {
        this.host = hst;
        this.acc = access;
        this.sec = scrt;
        if (bckt == null) {
            this.buckt = hst;
        } else {
            this.buckt = bckt;
        }
        this.regn = rgn;
        if (syslg.isEmpty()) {
            this.slog = "syslog.s3auth.com:514";
        } else {
            this.slog = syslg;
        }
    }

    @Override
    public String name() {
        return this.host;
    }

    @Override
    public String key() {
        return this.acc;
    }

    @Override
    public String secret() {
        return this.sec;
    }

    @Override
    public String bucket() {
        return this.buckt;
    }

    @Override
    public String region() {
        return this.regn;
    }

    @Override
    public String syslog() {
        return this.slog;
    }

}
