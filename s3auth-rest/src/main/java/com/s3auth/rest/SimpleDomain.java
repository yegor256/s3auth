/*
 * Copyright (c) 2012-2024, Yegor Bugayenko
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the s3auth.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
