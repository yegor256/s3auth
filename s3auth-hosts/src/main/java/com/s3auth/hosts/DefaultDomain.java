/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Default implementation of domain.
 *
 * @since 0.0.1
 */
@SuppressWarnings("PMD.TooManyMethods")
@Immutable
@EqualsAndHashCode(of = "origin")
@Loggable(Loggable.DEBUG)
final class DefaultDomain implements Domain {

    /**
     * The original domain.
     */
    private final transient Domain origin;

    /**
     * Public ctor.
     * @param name Name of it
     * @param key Key of it
     * @param secret Secret of it
     * @param bucket Bucket of it.
     * @param region The region of S3 bucket
     * @param syslog Syslog host and port of the domain
     * @checkstyle ParameterNumber (3 lines)
     */
    DefaultDomain(@NotNull final String name, @NotNull final String key,
        @NotNull final String secret, @NotNull final String bucket,
        @NotNull final String region, @NotNull final String syslog) {
        this(
            // @checkstyle AnonInnerLength (50 lines)
            new Domain() {
                @Override
                public String toString() {
                    return String.format("%s/%s", name, region);
                }

                @Override
                public String name() {
                    return name;
                }

                @Override
                public String key() {
                    return key;
                }

                @Override
                public String secret() {
                    return secret;
                }

                @Override
                public String bucket() {
                    return bucket;
                }

                @Override
                public String region() {
                    return region;
                }

                @Override
                public String syslog() {
                    return syslog;
                }
            }
        );
    }

    /**
     * Public ctor.
     * @param domain The domain
     */
    DefaultDomain(@NotNull final Domain domain) {
        this.origin = domain;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    @NotNull
    public String name() {
        return this.origin.name().trim();
    }

    @Override
    @NotNull
    public String key() {
        return this.origin.key().trim();
    }

    @Override
    @NotNull
    public String secret() {
        return this.origin.secret().trim();
    }

    @Override
    @NotNull
    public String bucket() {
        return this.origin.bucket().trim();
    }

    @Override
    @NotNull
    public String region() {
        return this.origin.region().trim();
    }

    @Override
    public String syslog() {
        return this.origin.syslog().trim();
    }

}
