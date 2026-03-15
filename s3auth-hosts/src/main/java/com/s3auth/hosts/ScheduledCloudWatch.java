/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.manifests.Manifests;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

/**
 * This class obtains local stats and posts it to Amazon CloudWatch every hour.
 *
 * <p>The class is mutable and NOT thread-safe.</p>
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
@ScheduleWithFixedDelay(delay = 1, unit = TimeUnit.HOURS)
@SuppressWarnings("PMD.DoNotUseThreads")
public final class ScheduledCloudWatch implements Runnable, Closeable {

    /**
     * Domain stats data to post to CloudWatch.
     */
    private final transient DomainStatsData data;

    /**
     * Amazon CloudWatch client.
     */
    private final transient CloudWatchClient cloudwatch;

    /**
     * Ctor.
     * @param stats The stats data to obtain.
     * @param cwatch The Cloudwatch client.
     */
    ScheduledCloudWatch(final DomainStatsData stats,
        final CloudWatchClient cwatch) {
        this.cloudwatch = cwatch;
        this.data = stats;
    }

    /**
     * Public ctor.
     * @throws IOException If an IO Exception occurs
     */
    public ScheduledCloudWatch() throws IOException {
        this(
            new H2DomainStatsData(),
            CloudWatchClient.builder()
                .credentialsProvider(
                    StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                            Manifests.read("S3Auth-AwsCloudWatchKey"),
                            Manifests.read("S3Auth-AwsCloudWatchSecret")
                        )
                    )
                )
                .build()
        );
    }

    @Override
    public void run() {
        try {
            final Map<String, Stats> results = this.data.all();
            for (final Entry<String, Stats> entry : results.entrySet()) {
                this.putData(entry.getValue(), entry.getKey());
            }
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close() {
        // Nothing to do, ScheduleWithFixedDelay will stop the thread.
    }

    /**
     * Put data in Amazon Cloudwatch.
     * @param stats The stats to put
     * @param bucket The bucket corresponding to these stats
     */
    @RetryOnFailure(delay = 5L, unit = TimeUnit.SECONDS)
    private void putData(final Stats stats, final String bucket) {
        this.cloudwatch.putMetricData(
            PutMetricDataRequest.builder()
                .namespace("S3Auth")
                .metricData(
                    MetricDatum.builder()
                        .metricName("BytesTransferred")
                        .dimensions(
                            Dimension.builder()
                                .name("Bucket")
                                .value(bucket)
                                .build()
                        )
                        .unit(StandardUnit.BYTES)
                        .value((double) stats.bytesTransferred())
                        .build()
                )
                .build()
        );
    }

}
