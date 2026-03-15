/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

/**
 * Test case for {@link ScheduledCloudWatch}.
 * @since 0.0.1
 */
final class ScheduledCloudWatchTest {

    /**
     * If an exception occurs.
     * @throws Exception If something goes wrong.
     */
    @Test
    void postsDataToCloudWatch() throws Exception {
        final String bucket = "bucket";
        final long bytes = 1_000_000;
        final DomainStatsData data = Mockito.mock(DomainStatsData.class);
        Mockito.doReturn(
            Collections.singletonMap(bucket, new Stats.Simple(bytes))
        ).when(data).all();
        final CloudWatchClient client = Mockito.mock(CloudWatchClient.class);
        final ScheduledCloudWatch cloudwatch =
            new ScheduledCloudWatch(data, client);
        cloudwatch.run();
        Mockito.verify(client).putMetricData(
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
                        ).unit(StandardUnit.BYTES)
                        .value(Double.valueOf(bytes))
                        .build()
                )
                .build()
        );
        cloudwatch.close();
    }

}
