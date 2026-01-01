/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        final AmazonCloudWatch client = Mockito.mock(AmazonCloudWatch.class);
        final ScheduledCloudWatch cloudwatch =
            new ScheduledCloudWatch(data, client);
        cloudwatch.run();
        Mockito.verify(client).putMetricData(
            new PutMetricDataRequest()
                .withNamespace("S3Auth")
                .withMetricData(
                    new MetricDatum()
                        .withMetricName("BytesTransferred")
                        .withDimensions(
                            new Dimension()
                                .withName("Bucket")
                                .withValue(bucket)
                        ).withUnit(StandardUnit.Bytes)
                        .withValue(Double.valueOf(bytes))
                )
        );
        cloudwatch.close();
    }

}
