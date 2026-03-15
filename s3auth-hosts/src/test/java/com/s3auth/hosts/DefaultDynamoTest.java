/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.dynamo.Table;
import com.jcabi.dynamo.mock.H2Data;
import com.jcabi.dynamo.mock.MkRegion;
import com.jcabi.urn.URN;
import com.jcabi.urn.URNMocker;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Test case for {@link DefaultDynamo}.
 * @since 0.0.1
 */
final class DefaultDynamoTest {

    @Test
    void loadsDynamoConfiguration() throws Exception {
        final String table = "table";
        final Dynamo dynamo = new DefaultDynamo(
            this.mockRegion(table),
            table
        );
        final int size = dynamo.load().size();
        MatcherAssert.assertThat(
            dynamo.load().size(),
            Matchers.is(size)
        );
        dynamo.add(new URN("urn:test:alpha"), new DomainMocker().init().mock());
        MatcherAssert.assertThat(
            dynamo.load().size(),
            Matchers.is(size + 1)
        );
        dynamo.close();
    }

    /**
     * DefaultDynamo can instantiate from default constructor.
     */
    @Test
    void instantiatesDefault() {
        MatcherAssert.assertThat(
            new DefaultDynamo(),
            Matchers.not(Matchers.nullValue())
        );
    }

    /**
     * Create and return a MkRegion with 20 random items in the given table.
     * @param table Table
     * @return The MkRegion
     * @throws IOException If there is some problem inside
     */
    @NotNull
    private MkRegion mockRegion(
        @NotNull(message = "table is never null") final String table)
        throws IOException {
        final MkRegion region = new MkRegion(
            new H2Data().with(
                table,
                new String[] {DefaultDynamo.USER},
                DefaultDynamo.NAME,
                DefaultDynamo.KEY,
                DefaultDynamo.SECRET,
                DefaultDynamo.BUCKET,
                DefaultDynamo.REGION,
                DefaultDynamo.SYSLOG
            )
        );
        final Table tbl = region.table(table);
        for (int num = 0; num < 3; ++num) {
            tbl.put(this.item());
        }
        return region;
    }

    /**
     * Create and return a random amazon item.
     * @return The client
     */
    private Map<String, AttributeValue> item() {
        final ConcurrentMap<String, AttributeValue> item =
            new ConcurrentHashMap<>(0);
        item.put(
            DefaultDynamo.USER,
            AttributeValue.builder().s(new URNMocker().mock().toString()).build()
        );
        item.put(
            DefaultDynamo.NAME,
            AttributeValue.builder().s(
                String.format(
                    "google-%d.com",
                    Math.abs(new Random().nextInt())
                )
            ).build()
        );
        item.put(DefaultDynamo.KEY, AttributeValue.builder().s("aaaaaaaaaaaaaaaa").build());
        item.put(
            DefaultDynamo.SECRET,
            AttributeValue.builder().s("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa").build()
        );
        item.put(DefaultDynamo.REGION, AttributeValue.builder().s("s3").build());
        item.put(DefaultDynamo.BUCKET, AttributeValue.builder().s("bucket").build());
        item.put(DefaultDynamo.SYSLOG, AttributeValue.builder().s("syslog").build());
        return item;
    }
}
