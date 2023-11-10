/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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
package com.s3auth.hosts;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.HttpStatus;

/**
 * Default implementation of {@link Host}.
 *
 * @since 0.0.1
 * @checkstyle ClassFanOutComplexityCheck (1000 lines)
 */
@Immutable
@EqualsAndHashCode(of = "bucket")
@Loggable(Loggable.DEBUG)
@SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveImports" })
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
        public AmazonCloudWatch get() {
            return AmazonCloudWatchAsyncClientBuilder.standard()
                .withExecutorFactory(() -> Executors.newFixedThreadPool(50))
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(
                    new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
                            Manifests.read("S3Auth-AwsCloudWatchKey"),
                            Manifests.read("S3Auth-AwsCloudWatchSecret")
                        )
                    )
                )
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
        this.statistics = new HostStats(this.bucket.bucket());
    }

    @Override
    public String toString() {
        return this.bucket.toString();
    }

    @Override
    public void close() {
        // nothing to do
    }

    // @checkstyle CyclomaticComplexity (100 lines)
    // @checkstyle ExecutableStatementCount (100 lines)
    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Loggable(value = Loggable.DEBUG, ignore = IOException.class)
    public Resource fetch(@NotNull final URI uri,
        @NotNull final Range range, @NotNull final Version version)
        throws IOException {
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
        Resource resource = null;
        final Collection<String> errors = new LinkedList<>();
        final DomainStatsData data = new H2DomainStatsData().init();
        for (final DefaultHost.ObjectName name : this.names(uri)) {
            try {
                if (version.list()) {
                    resource = new ObjectVersionListing(
                        this.bucket.client(), this.bucket.bucket(), name.get()
                    );
                } else {
                    resource = new DefaultResource(
                        this.bucket.client(), this.bucket.bucket(),
                        name.get(), range, version, data
                    );
                }
                break;
            } catch (final AmazonServiceException ex) {
                if (StringUtils.endsWith(name.get(), DefaultHost.SUFFIX)
                    && "NoSuchKey".equals(ex.getErrorCode())
                ) {
                    resource = new DirectoryListing(
                        this.bucket.client(), this.bucket.bucket(),
                        StringUtils.removeEnd(name.get(), DefaultHost.SUFFIX)
                    );
                    break;
                } else if ("NoSuchBucket".equals(ex.getErrorCode())) {
                    throw new IOException(
                        Logger.format(
                            "The bucket '%s' does not exist.",
                            this.bucket.bucket()
                        ),
                        ex
                    );
                } else if (ex.getStatusCode() >= HttpStatus.SC_BAD_REQUEST
                    && ex.getStatusCode() < HttpStatus.SC_INTERNAL_SERVER_ERROR
                ) {
                    try {
                        final BucketWebsiteConfiguration config =
                            this.bucket.client().getBucketWebsiteConfiguration(
                                this.bucket.bucket()
                            );
                        if (config != null
                            && config.getErrorDocument() != null) {
                            resource = new DefaultResource(
                                this.bucket.client(), this.bucket.bucket(),
                                config.getErrorDocument(), Range.ENTIRE,
                                Version.LATEST, data
                            );
                        }
                    } catch (final AmazonClientException exc) {
                        errors.add(
                            String.format("'%s': %s", name, exc.getMessage())
                        );
                    }
                }
                errors.add(String.format("'%s': %s", name, ex.getMessage()));
            } catch (final AmazonClientException ex) {
                errors.add(String.format("'%s': %s", name, ex.getMessage()));
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

    /**
     * Convert URI to all possible S3 object names (in order of importance).
     * @param uri The URI
     * @return Object names
     * @checkstyle NonStaticMethodCheck (20 lines)
     */
    private Iterable<DefaultHost.ObjectName> names(final URI uri) {
        final String name = StringUtils.strip(uri.getPath(), "/");
        final Collection<DefaultHost.ObjectName> names =
            new LinkedList<>();
        if (!name.isEmpty()) {
            names.add(new DefaultHost.Simple(name));
        }
        names.add(new DefaultHost.NameWithSuffix(name));
        return names;
    }

    /**
     * Object name with a suffix from a bucket.
     *
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
                final BucketWebsiteConfiguration conf =
                    DefaultHost.this.bucket.client()
                        .getBucketWebsiteConfiguration(
                            DefaultHost.this.bucket.name()
                        );
                if (conf != null) {
                    suffix = conf.getIndexDocumentSuffix();
                }
            } catch (final AmazonClientException ex) {
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
     * Object name.
     *
     * @since 0.0.1
     */
    @EqualsAndHashCode(of = "name")
    private static final class Simple implements DefaultHost.ObjectName {
        /**
         * Original name.
         */
        private final transient String name;

        /**
         * Public ctor.
         * @param nme The name
         */
        Simple(final String nme) {
            this.name = nme;
        }

        @Override
        public String get() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    /**
     * Stats for this domain.
     *
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    @EqualsAndHashCode(of = "bucket")
    private final class HostStats implements Stats {
        /**
         * The S3 bucket.
         */
        private final transient String bucket;

        /**
         * Public ctor.
         * @param bckt The name of the bucket
         */
        HostStats(final String bckt) {
            this.bucket = bckt;
        }

        @Override
        @Cacheable(lifetime = 30, unit = TimeUnit.MINUTES)
        public long bytesTransferred() {
            final Date now = new Date();
            final List<Datapoint> datapoints =
                DefaultHost.this.cloudwatch.get().getMetricStatistics(
                    new GetMetricStatisticsRequest()
                        .withMetricName("BytesTransferred")
                        .withNamespace("S3Auth")
                        .withStatistics("Sum")
                        .withDimensions(
                            new Dimension()
                                .withName("Bucket")
                                .withValue(this.bucket)
                        )
                        .withUnit(StandardUnit.Bytes)
                        .withPeriod((int) TimeUnit.DAYS.toSeconds(7))
                        .withStartTime(DateUtils.addWeeks(now, -1))
                        .withEndTime(now)
                ).getDatapoints();
            long sum = 0L;
            for (final Datapoint datapoint : datapoints) {
                sum += datapoint.getSum();
            }
            return sum;
        }
    }

    /**
     * Name of an S3 Object, context dependent.
     *
     * @since 0.0.1
     */
    private interface ObjectName {
        /**
         * Returns a name of S3 object.
         * @return The name
         */
        String get();
    }

}
