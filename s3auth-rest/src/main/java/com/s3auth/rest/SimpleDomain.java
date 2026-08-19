/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import com.s3auth.hosts.Domain;

/**
 * Simple domain.
 * @since 0.1
 */
final class SimpleDomain implements Domain {

    /**
     * Default syslog host and port, used when none was set explicitly.
     */
    private static final String SYSLOG = "syslog.s3auth.com:514";

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
        this(hst, "", "", "", "", SimpleDomain.SYSLOG);
    }

    /**
     * Constructor.
     * @param hst The host name
     * @param access AWS access key
     * @param scrt AWS secret
     * @param bckt Bucket name
     * @param rgn S3 region
     * @param syslg The syslog host and port
     */
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
        this.slog = syslg;
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

    /**
     * Syslog host and port to use, falling back to the default one
     * when the given value is empty.
     * @param syslg Syslog host and port given, may be empty
     * @return Syslog host and port to use
     */
    static String syslog(final String syslg) {
        final String value;
        if (syslg.isEmpty()) {
            value = SimpleDomain.SYSLOG;
        } else {
            value = syslg;
        }
        return value;
    }
}
