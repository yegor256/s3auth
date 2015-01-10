/**
 * Copyright (c) 2012-2015, s3auth.com
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

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.jcabi.aspects.Tv;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link ScheduledCloudWatch}.
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
public final class ScheduledCloudWatchTest {

    /**
     * If an exception occurs.
     * @throws Exception If something goes wrong.
     */
    @Test
    public void postsDataToCloudWatch() throws Exception {
        final String bucket = "bucket";
        final long bytes = Tv.MILLION;
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
