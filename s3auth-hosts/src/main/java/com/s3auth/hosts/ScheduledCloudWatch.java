/**
 * Copyright (c) 2012-2014, s3auth.com
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

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.RetryOnFailure;
import com.jcabi.aspects.ScheduleWithFixedDelay;
import com.jcabi.aspects.Tv;
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
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
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
            new AmazonCloudWatchClient(
                new BasicAWSCredentials(
                    Manifests.read("S3Auth-AwsCloudWatchKey"),
                    Manifests.read("S3Auth-AwsCloudWatchSecret")
                ),
                new ClientConfiguration().withProtocol(Protocol.HTTP)
            )
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
    @RetryOnFailure(delay = Tv.FIVE, unit = TimeUnit.SECONDS)
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
