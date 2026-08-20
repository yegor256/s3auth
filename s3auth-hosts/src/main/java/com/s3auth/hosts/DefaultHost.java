/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Datapoint;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.GetMetricStatisticsRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;
import software.amazon.awssdk.services.cloudwatch.model.Statistic;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteRequest;
import software.amazon.awssdk.services.s3.model.GetBucketWebsiteResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Default implementation of {@link Host}.
 * @since 0.0.1
 * @checkstyle NonStaticMethodCheck (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
final class DefaultHost implements Host {

    /**
     * The suffix index.html.
     */
    private static final String SUFFIX = "index.html";

    /**
     * Caching Host.CloudWatch instance.
     */
    private static final Host.CloudWatch CLOUDWATCH = new Host.CloudWatch() {
        @Override
        @Cacheable(lifetime = 1, unit = TimeUnit.HOURS)
        public CloudWatchClient get() {
            return CloudWatchClient.builder()
                .credentialsProvider(DefaultHost.credentials())
                .build();
        }
    };

    /**
     * The S3 bucket.
     */
    private final transient Bucket bucket;

    /**
     * Htpasswd file abstraction.
     */
    private final transient Htpasswd htpasswd;

    /**
     * Holder of host stats.
     */
    private final transient Stats statistics;

    /**
     * Amazon Cloudwatch Client.
     */
    private final transient Host.CloudWatch cloudwatch;

    /**
     * Public ctor.
     * @param bckt The S3 bucket to use
     */
    DefaultHost(@NotNull final Bucket bckt) {
        this(
            bckt,
            DefaultHost.CLOUDWATCH
        );
    }

    /**
     * Ctor for unit tests.
     * @param bckt The S3 bucket to use
     * @param cwatch The Amazon Cloudwatch client
     */
    DefaultHost(
        @NotNull final Bucket bckt,
        @NotNull final Host.CloudWatch cwatch
    ) {
        this.bucket = bckt;
        this.htpasswd = new Htpasswd(this);
        this.cloudwatch = cwatch;
        this.statistics = new DefaultHost.HostStats(this.bucket);
    }

    @Override
    public String toString() {
        return this.bucket.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.bucket);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof DefaultHost
            && Objects.equals(this.bucket, ((DefaultHost) obj).bucket);
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    @NotNull
    @Loggable(value = Loggable.DEBUG, ignore = IOException.class)
    public Resource fetch(@NotNull final URI uri,
        @NotNull final Range range, @NotNull final Version version)
        throws IOException {
        this.validate();
        Resource resource = null;
        final Collection<String> errors = new ArrayList<>(2);
        final DomainStatsData data = new H2DomainStatsData().init();
        for (final DefaultHost.ObjectName name : this.names(uri)) {
            final Attempt outcome =
                this.attempt(name, range, version, data, errors);
            if (outcome.resource() != null) {
                resource = outcome.resource();
            }
            if (outcome.done()) {
                break;
            }
        }
        if (resource == null) {
            throw new IOException(
                Logger.format(
                    "Failed to fetch %s from '%s' (key=%s): %[list]s",
                    uri, this.bucket.name(), this.bucket.key(), errors
                )
            );
        }
        return resource;
    }

    @Override
    public boolean isHidden(@NotNull final URI uri) {
        return true;
    }

    @Override
    public boolean authorized(@NotNull final String user,
        @NotNull final String password) throws IOException {
        final boolean auth;
        if (user.equals(this.bucket.key())
            && password.equals(this.bucket.secret())) {
            auth = true;
        } else {
            auth = this.htpasswd.authorized(user, password);
        }
        return auth;
    }

    @Override
    public String syslog() {
        return this.bucket.syslog();
    }

    @Override
    public Stats stats() {
        return this.statistics;
    }

    private Attempt attempt(final DefaultHost.ObjectName name,
        final Range range, final Version version, final DomainStatsData data,
        final Collection<String> errors) throws IOException {
        Resource resource = null;
        boolean done = false;
        try {
            resource = DefaultHost.fetchOne(this.bucket, name, range, version, data);
            done = true;
        } catch (final NoSuchBucketException ex) {
            throw new IOException(
                Logger.format(
                    "The bucket '%s' does not exist.",
                    this.bucket.bucket()
                ),
                ex
            );
        } catch (final NoSuchKeyException ex) {
            if (name.get().endsWith(DefaultHost.SUFFIX)) {
                final String path = name.get();
                resource = DirectoryListing.fetch(
                    this.bucket.client(), this.bucket.bucket(),
                    path.substring(0, path.length() - DefaultHost.SUFFIX.length())
                );
                done = true;
            } else {
                errors.add(String.format("'%s': %s", name, ex.getMessage()));
            }
        } catch (final S3Exception ex) {
            resource = this.errorDocument(name, data, ex, errors);
        }
        return new Attempt(resource, done);
    }

    private static Resource fetchOne(final Bucket bckt,
        final DefaultHost.ObjectName name, final Range range,
        final Version version, final DomainStatsData data) {
        final Resource resource;
        if (version.list()) {
            resource = ObjectVersionListing.fetch(
                bckt.client(), bckt.bucket(), name.get()
            );
        } else {
            resource = DefaultResource.fetch(
                bckt.client(),
                new Locator(
                    bckt.bucket(), name.get(), range, version
                ),
                data
            );
        }
        return resource;
    }

    private void validate() {
        if (this.bucket.key().isEmpty()) {
            throw new IllegalStateException(
                "The key of the bucket is empty"
            );
        }
        if (this.bucket.secret().isEmpty()) {
            throw new IllegalStateException(
                "The secret of the bucket is empty"
            );
        }
    }

    private Resource errorDocument(final DefaultHost.ObjectName name,
        final DomainStatsData data, final S3Exception err,
        final Collection<String> errors) {
        Resource resource = null;
        if (err.statusCode() >= HttpURLConnection.HTTP_BAD_REQUEST
            && err.statusCode() < HttpURLConnection.HTTP_INTERNAL_ERROR
        ) {
            try {
                final GetBucketWebsiteResponse config =
                    this.bucket.client().getBucketWebsite(
                        GetBucketWebsiteRequest.builder()
                            .bucket(this.bucket.bucket())
                            .build()
                    );
                if (config != null
                    && config.errorDocument() != null
                    && config.errorDocument().key() != null) {
                    resource = DefaultResource.fetch(
                        this.bucket.client(),
                        new Locator(
                            this.bucket.bucket(), config.errorDocument().key(),
                            Range.ENTIRE, Version.LATEST
                        ),
                        data
                    );
                }
            } catch (final S3Exception exc) {
                errors.add(
                    String.format("'%s': %s", name, exc.getMessage())
                );
            }
        }
        errors.add(String.format("'%s': %s", name, err.getMessage()));
        return resource;
    }

    private Iterable<DefaultHost.ObjectName> names(final URI uri) {
        final String name = StringUtils.strip(uri.getPath(), "/");
        final Collection<DefaultHost.ObjectName> names =
            new ArrayList<>(2);
        if (!name.isEmpty()) {
            names.add(new SimpleObjectName(name));
        }
        names.add(new DefaultHost.NameWithSuffix(name));
        return names;
    }

    private GetBucketWebsiteResponse website() {
        return this.bucket.client().getBucketWebsite(
            GetBucketWebsiteRequest.builder()
                .bucket(this.bucket.name())
                .build()
        );
    }

    private static StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(
                Manifests.read("S3Auth-AwsCloudWatchKey"),
                Manifests.read("S3Auth-AwsCloudWatchSecret")
            )
        );
    }

    /**
     * Object name with a suffix from a bucket.
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private final class NameWithSuffix implements DefaultHost.ObjectName {

        /**
         * Original name.
         */
        private final transient String origin;

        /**
         * Public ctor.
         * @param name The original name
         */
        NameWithSuffix(final String name) {
            this.origin = name;
        }

        @Override
        public String get() {
            String suffix = null;
            try {
                final GetBucketWebsiteResponse conf = DefaultHost.this.website();
                if (conf != null && conf.indexDocument() != null) {
                    suffix = conf.indexDocument().suffix();
                }
            } catch (final S3Exception ex) {
                suffix = "";
            }
            if (suffix == null || suffix.isEmpty()) {
                suffix = DefaultHost.SUFFIX;
            }
            final StringBuilder text = new StringBuilder(this.origin);
            if (text.length() > 0) {
                text.append('/');
            }
            text.append(suffix);
            return text.toString();
        }

        @Override
        public String toString() {
            return String.format("%s+suffix", this.origin);
        }
    }

    /**
     * Stats for this domain.
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private final class HostStats implements Stats {

        /**
         * The S3 bucket.
         */
        private final transient Bucket bucket;

        /**
         * Public ctor.
         * @param bckt The bucket
         */
        HostStats(final Bucket bckt) {
            this.bucket = bckt;
        }

        @Override
        @Cacheable(lifetime = 30, unit = TimeUnit.MINUTES)
        public long bytesTransferred() {
            final Instant now = Instant.now();
            final List<Datapoint> datapoints =
                DefaultHost.this.cloudwatch.get().getMetricStatistics(
                    GetMetricStatisticsRequest.builder()
                        .metricName("BytesTransferred")
                        .namespace("S3Auth")
                        .statistics(Statistic.SUM).dimensions(
                            Dimension.builder()
                                .name("Bucket")
                                .value(this.bucket.bucket())
                                .build()
                        )
                        .unit(StandardUnit.BYTES)
                        .period((int) TimeUnit.DAYS.toSeconds(7))
                        .startTime(now.minus(7, java.time.temporal.ChronoUnit.DAYS))
                        .endTime(now)
                        .build()
                ).datapoints();
            long sum = 0L;
            for (final Datapoint datapoint : datapoints) {
                sum += datapoint.sum().longValue();
            }
            return sum;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.bucket);
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof HostStats
                && Objects.equals(this.bucket, ((HostStats) obj).bucket);
        }
    }

    /**
     * Name of an S3 Object, context dependent.
     * @since 0.0.1
     */
    @FunctionalInterface
    interface ObjectName {

        /**
         * Returns a name of S3 object.
         * @return The name
         */
        String get();
    }
}
