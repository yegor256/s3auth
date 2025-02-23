/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.manifests.Manifests;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

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
    private final transient AmazonCloudWatch cloudwatch;

    /**
     * Ctor.
     * @param stats The stats data to obtain.
     * @param cwatch The Cloudwatch client.
     */
    ScheduledCloudWatch(final DomainStatsData stats,
        final AmazonCloudWatch cwatch) {
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
            AmazonCloudWatchAsyncClientBuilder.standard()
                .withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP))
                .withCredentials(
                    new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials(
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
            new PutMetricDataRequest()
                .withNamespace("S3Auth")
                .withMetricData(
                    new MetricDatum()
                        .withMetricName("BytesTransferred")
                        .withDimensions(
                            new Dimension()
                                .withName("Bucket")
                                .withValue(bucket)
                        )
                        .withUnit(StandardUnit.Bytes)
                        .withValue((double) stats.bytesTransferred())
                )
        );
    }

}
