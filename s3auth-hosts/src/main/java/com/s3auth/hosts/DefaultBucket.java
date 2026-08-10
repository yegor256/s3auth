/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Default implementation of {@link Bucket}.
 * @since 0.0.1
 */
@Immutable
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
    public S3Client client() {
        return S3Client.builder()
            .region(this.awsRegion())
            .credentialsProvider(this.credentials())
            .build();
    }

    @Override
    public String toString() {
        return this.domain.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.domain);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof DefaultBucket
            && Objects.equals(this.domain, ((DefaultBucket) obj).domain);
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

    /**
     * AWS credentials, built from the domain's key and secret.
     * @return Credentials provider for the AWS SDK
     */
    private StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                this.domain.key(),
                this.domain.secret()
            )
        );
    }

    /**
     * Translate a legacy S3 region/endpoint string into a SDK v2 {@link Region}.
     * Old s3auth records carry values like {@code s3}, {@code s3-eu-west-1},
     * {@code s3-website-us-east-1} or {@code s3.amazonaws.com}, none of which
     * the v2 SDK understands. This method strips the legacy prefixes and
     * suffixes and returns the underlying region id.
     * @return Region for the AWS SDK
     */
    private Region awsRegion() {
        String raw = this.domain.region().trim();
        if (raw.endsWith(".amazonaws.com")) {
            raw = raw.substring(0, raw.length() - ".amazonaws.com".length());
        }
        final int idx = raw.indexOf("s3");
        if (idx > 0) {
            raw = raw.substring(idx);
        }
        final String id;
        if ("s3".equals(raw) || "s3-external-1".equals(raw)) {
            id = "us-east-1";
        } else if (raw.startsWith("s3-website-")) {
            id = raw.substring("s3-website-".length());
        } else if (raw.startsWith("s3-")) {
            id = raw.substring("s3-".length());
        } else {
            id = raw;
        }
        return Region.of(id);
    }
}
