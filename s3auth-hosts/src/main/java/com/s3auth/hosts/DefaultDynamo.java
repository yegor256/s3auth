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

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import com.jcabi.dynamo.Attributes;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.Table;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Abstraction on top of DynamoDB SDK.
 *
 * <p>The class is mutable and thread-safe.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @todo #1 Would be nice to migrate to jcabi-dynamo
 * @since 0.0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = {"table" })
@Loggable(Loggable.INFO)
final class DefaultDynamo implements Dynamo {

    /**
     * Dynamo DB key, URN of a user.
     */
    public static final String USER = "user_urn";

    /**
     * Dynamo DB key, name of domain.
     */
    public static final String NAME = "domain_name";

    /**
     * Dynamo DB key, AWS key of bucket.
     */
    public static final String KEY = "domain_key";

    /**
     * Dynamo DB key, AWS secret of bucket.
     */
    public static final String SECRET = "domain_secret";

    /**
     * Dynamo DB key, Name of bucket.
     */
    public static final String BUCKET = "domain_bucket";

    /**
     * Dynamo DB key, AWS S3 region of bucket.
     */
    public static final String REGION = "domain_region";

    /**
     * Dynamo DB key, Syslog host and port of domain.
     */
    public static final String SYSLOG = "domain_syslog";

    /**
     * Name of the entry point setting.
     */
    public static final String ENTRY_POINT =
        "S3Auth-AwsDynamoEntryPoint";

    /**
     * Table name.
     */
    private final transient String table;

    /**
     * Region factory.
     */
    private final transient RegionCreator regionfactory;

    /**
     * Public ctor.
     */
    DefaultDynamo() {
        this.regionfactory = new ReRegionCreator(
            new Credentials.Simple(
                Manifests.read("S3Auth-AwsDynamoKey"),
                Manifests.read("S3Auth-AwsDynamoSecret")
            )
        );
        final AmazonDynamoDB aws = this.regionfactory.aws();
        if (Manifests.exists(ENTRY_POINT)) {
            aws.setEndpoint(
                Manifests.read(ENTRY_POINT)
            );
        }
        this.table = Manifests.read("S3Auth-AwsDynamoTable");
    }

    /**
     * Ctor for unit tests.
     * @param regfactory Region factory
     * @param tbl Table name
     */
    DefaultDynamo(@NotNull final RegionCreator regfactory,
        @NotNull final String tbl) {
        this.regionfactory = regfactory;
        this.table = tbl;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    public ConcurrentMap<URN, Domains> load() {
        final ConcurrentMap<URN, Domains> domains =
            new ConcurrentHashMap<URN, Domains>(0);
        final Region region = this.regionfactory.createRegion();
        try {
            final Iterator<Item> items = region.table(this.table).frame()
                .iterator();
            while (items.hasNext()) {
                final Item item = items.next();
                final String syslog;
                if (item.has(DefaultDynamo.SYSLOG)) {
                    syslog = item.get(DefaultDynamo.SYSLOG).getS();
                } else {
                    syslog = "syslog.s3auth.com:514";
                }
                final String bucket;
                if (item.has(DefaultDynamo.BUCKET)) {
                    bucket = item.get(DefaultDynamo.BUCKET).getS();
                } else {
                    bucket = item.get(DefaultDynamo.NAME).getS();
                }
                final URN user =
                    URN.create(item.get(DefaultDynamo.USER).getS());
                domains.putIfAbsent(user, new Domains());
                domains.get(user).add(
                    new DefaultDomain(
                        item.get(DefaultDynamo.NAME).getS(),
                        item.get(DefaultDynamo.KEY).getS(),
                        item.get(DefaultDynamo.SECRET).getS(),
                        bucket,
                        item.get(DefaultDynamo.REGION).getS(),
                        syslog
                    )
                );
            }
        } catch (final IOException exception) {
            Logger.error(this, exception.getMessage());
        }
        return domains;
    }

    @Override
    @Cacheable.FlushBefore
    public boolean add(@NotNull final URN user,
        @NotNull final Domain domain) {
        final Region region = this.regionfactory.createRegion();
        final Table curTable = region.table(this.table);
        boolean success = false;
        try {
            curTable.put(new Attributes()
                .with(
                    DefaultDynamo.USER,
                    new AttributeValue(
                        user.toString()
                    )
            )
                .with(
                    DefaultDynamo.NAME,
                    new AttributeValue(
                        domain.name()
                    )
                )
                .with(
                    DefaultDynamo.KEY,
                    new AttributeValue(
                        domain.key()
                    )
                )
                .with(
                    DefaultDynamo.SECRET,
                    new AttributeValue(
                        domain.secret()
                    )
                )
                .with(
                    DefaultDynamo.REGION,
                    new AttributeValue(
                        domain.region()
                    )
                )
                .with(
                    DefaultDynamo.SYSLOG,
                    new AttributeValue(
                        domain.syslog()
                    )
                )
                .with(
                    DefaultDynamo.BUCKET,
                    new AttributeValue(
                        domain.bucket()
                    )
                )
            );
            success = true;
        } catch (final IOException exception) {
            Logger.error(this, exception.getMessage());
        }
        return success;
    }

    @Override
    @Cacheable.FlushBefore
    public boolean remove(@NotNull final Domain domain) {
        final Region region = this.regionfactory.createRegion();
        final Table curTable = region.table(this.table);
        final Iterator<Item> itemsToRemove = curTable.frame().where(
            DefaultDynamo.NAME,
            new Condition().withComparisonOperator(ComparisonOperator.EQ)
        ).iterator();
        while (itemsToRemove.hasNext()) {
            itemsToRemove.next();
            itemsToRemove.remove();
        }
        return true;
    }
}
