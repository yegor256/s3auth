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

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.mock.H2Data;
import com.jcabi.dynamo.mock.MkRegion;
import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DefaultDynamo}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DefaultDynamoTest {
    /**
     * Test table name.
     */
    public static final String TABLE = "table";

    /**
     * DefaultDynamo can load configuration.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void loadsDynamoConfiguration() throws Exception {
        final H2Data data = new H2Data().with(
            TABLE,
            new String[]
            {
                DefaultDynamo.USER,
            },
            new String[]
            {
                DefaultDynamo.NAME,
                DefaultDynamo.KEY,
                DefaultDynamo.SECRET,
                DefaultDynamo.REGION,
                DefaultDynamo.BUCKET,
                DefaultDynamo.SYSLOG,
            }
        );
        for (int num = 0; num < Tv.TWENTY; ++num) {
            data.put(
                TABLE,
                new Attributes(
                    this.item()
                )
            );
        }
        final Region region = new MkRegion(data);
        final Regions regions = Mockito.mock(Regions.class);
        Mockito.when(regions.create()).thenReturn(region);
        final DefaultDynamo dynamo = new DefaultDynamo(
            regions, TABLE
        );
        final int size = dynamo.load().size();
        MatcherAssert.assertThat(
            dynamo.load().size(),
            Matchers.is(size)
        );
        dynamo.add(new URN("urn:test:alpha"), new DomainMocker().mock());
        MatcherAssert.assertThat(
            dynamo.load().size(),
            Matchers.is(size + 1)
        );
        dynamo.close();
    }

    /**
     * Create and return a random amazon item.
     * @return The client
     */
    private Map<String, AttributeValue> item() {
        final ConcurrentMap<String, AttributeValue> item =
            new ConcurrentHashMap<String, AttributeValue>(0);
        item.put(
            DefaultDynamo.USER,
            new AttributeValue(new URNMocker().mock().toString())
        );
        item.put(
            DefaultDynamo.NAME,
            new AttributeValue(
                String.format(
                    "google-%d.com",
                    Math.abs(new Random().nextInt())
                )
            )
        );
        item.put(
            DefaultDynamo.KEY,
            new AttributeValue("aaaaaaaaaaaaaaaa")
        );
        item.put(
            DefaultDynamo.SECRET,
            new AttributeValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
        );
        item.put(DefaultDynamo.REGION, new AttributeValue("s3"));
        item.put(DefaultDynamo.BUCKET, new AttributeValue(""));
        item.put(DefaultDynamo.SYSLOG, new AttributeValue(""));
        return item;
    }
}
