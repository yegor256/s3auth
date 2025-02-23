/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Default implementation of {@link Bucket}.
 *
 * @since 0.0.1
 */
@Immutable
@EqualsAndHashCode(of = "domain")
@Loggable(Loggable.DEBUG)
final class DefaultBucket implements Bucket {

    /**
     * The domain.
     */
    private final transient Domain domain;

    /**
     * Public ctor.
     * @param dmn The domain
     */
    DefaultBucket(@NotNull final Domain dmn) {
        this.domain = dmn;
    }

    @Override
    @NotNull
    @Cacheable(lifetime = 10, unit = TimeUnit.MINUTES)
    public AmazonS3 client() {
        return AmazonS3ClientBuilder.standard()
            .withClientConfiguration(
                new ClientConfiguration()
                    .withSocketTimeout(0)
                    .withProtocol(Protocol.HTTPS)
            )
            .withRegion(this.domain.region())
            .withCredentials(
                new AWSStaticCredentialsProvider(
                    new BasicAWSCredentials(
                        this.domain.key(),
                        this.domain.secret()
                    )
                )
            )
            .build();
    }

    @Override
    public String toString() {
        return this.domain.toString();
    }

    @Override
    @NotNull
    public String name() {
        return this.domain.name();
    }

    @Override
    @NotNull
    public String key() {
        return this.domain.key();
    }

    @Override
    @NotNull
    public String secret() {
        return this.domain.secret();
    }

    @Override
    @NotNull
    public String bucket() {
        return this.domain.bucket();
    }

    @Override
    @NotNull
    public String region() {
        return this.domain.region();
    }

    @Override
    public String syslog() {
        return this.domain.syslog();
    }

}
