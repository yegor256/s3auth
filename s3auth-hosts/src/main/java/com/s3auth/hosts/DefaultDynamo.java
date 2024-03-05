/*
 * Copyright (c) 2012-2024, Yegor Bugayenko
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

import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.dynamo.AttributeUpdates;
import com.jcabi.dynamo.Credentials;
import com.jcabi.dynamo.Item;
import com.jcabi.dynamo.Region;
import com.jcabi.dynamo.ScanValve;
import com.jcabi.dynamo.retry.ReRegion;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Abstraction on top of DynamoDB SDK.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @since 0.0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "region", "table" })
@Loggable(Loggable.INFO)
final class DefaultDynamo implements Dynamo {

    /**
     * Dynamo DB key, URN of a user.
     */
    public static final String USER = "user.urn";

    /**
     * Dynamo DB key, name of domain.
     */
    public static final String NAME = "domain.name";

    /**
     * Dynamo DB key, AWS key of bucket.
     */
    public static final String KEY = "domain.key";

    /**
     * Dynamo DB key, AWS secret of bucket.
     */
    public static final String SECRET = "domain.secret";

    /**
     * Dynamo DB key, Name of bucket.
     */
    public static final String BUCKET = "domain.bucket";

    /**
     * Dynamo DB key, AWS S3 region of bucket.
     */
    public static final String REGION = "domain.region";

    /**
     * Dynamo DB key, Syslog host and port of domain.
     */
    public static final String SYSLOG = "domain.syslog";

    /**
     * Client.
     */
    private final transient Region region;

    /**
     * Table name.
     */
    private final transient String table;

    /**
     * Public ctor.
     */
    DefaultDynamo() {
        this(
            new ReRegion(new Region.Simple(DefaultDynamo.creds())),
            Manifests.read("S3Auth-AwsDynamoTable")
        );
    }

    /**
     * Ctor for unit tests.
     * @param rgn The jcabi-dynamo Region
     * @param tbl Table name
     */
    DefaultDynamo(@NotNull final Region rgn,
        @NotNull final String tbl) {
        this.region = rgn;
        this.table = tbl;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Cacheable(lifetime = 5)
    public ConcurrentMap<URN, Domains> load() throws IOException {
        final ConcurrentMap<URN, Domains> domains =
            new ConcurrentHashMap<>(0);
        final Iterable<Item> items = this.region
            .table(this.table)
            .frame()
            .through(
                new ScanValve().withAttributeToGet(
                    DefaultDynamo.USER,
                    DefaultDynamo.NAME,
                    DefaultDynamo.KEY,
                    DefaultDynamo.SECRET,
                    DefaultDynamo.BUCKET,
                    DefaultDynamo.REGION,
                    DefaultDynamo.SYSLOG
                ).withLimit(1_000_000)
            );
        for (final Item item : items) {
            final URN user = URN.create(item.get(DefaultDynamo.USER).getS());
            domains.putIfAbsent(user, new Domains());
            domains.get(user).add(DefaultDynamo.domain(item));
        }
        return domains;
    }

    @Override
    @Cacheable.FlushBefore
    public boolean add(@NotNull final URN user,
        @NotNull final Domain domain) throws IOException {
        final ConcurrentMap<String, AttributeValue> attrs =
            new ConcurrentHashMap<>(0);
        attrs.put(DefaultDynamo.USER, new AttributeValue(user.toString()));
        attrs.put(DefaultDynamo.NAME, new AttributeValue(domain.name()));
        attrs.put(DefaultDynamo.KEY, new AttributeValue(domain.key()));
        attrs.put(DefaultDynamo.SECRET, new AttributeValue(domain.secret()));
        attrs.put(DefaultDynamo.REGION, new AttributeValue(domain.region()));
        attrs.put(DefaultDynamo.SYSLOG, new AttributeValue(domain.syslog()));
        attrs.put(DefaultDynamo.BUCKET, new AttributeValue(domain.bucket()));
        this.region.table(this.table).put(attrs);
        return true;
    }

    @Override
    @Cacheable.FlushBefore
    public boolean remove(@NotNull final Domain domain) {
        final Iterator<Item> items = this.region.table(this.table).frame()
            .where(DefaultDynamo.NAME, domain.name())
            .through(new ScanValve().withLimit(1_000_000))
            .iterator();
        boolean removed = false;
        while (items.hasNext()) {
            items.next();
            items.remove();
            removed = true;
        }
        return removed;
    }

    /**
     * Create AWS credentials.
     * @return Creds
     */
    private static Credentials creds() {
        Credentials creds = new Credentials.Simple(
            Manifests.read("S3Auth-AwsDynamoKey"),
            Manifests.read("S3Auth-AwsDynamoSecret")
        );
        if (Manifests.exists("S3Auth-AwsDynamoEntryPoint")) {
            creds = new Credentials.Direct(
                Credentials.Simple.class.cast(creds),
                Manifests.read("S3Auth-AwsDynamoEntryPoint")
            );
        }
        return creds;
    }

    /**
     * Turn item into domain.
     * @param item Item
     * @return Domain
     * @throws IOException If fails
     */
    private static Domain domain(final Item item) throws IOException {
        if (!item.has(DefaultDynamo.SYSLOG)) {
            item.put(
                new AttributeUpdates().with(
                    DefaultDynamo.SYSLOG,
                    new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(
                            new AttributeValue().withS(
                                "syslog.s3auth.com:514"
                            )
                        )
                )
            );
        }
        if (!item.has(DefaultDynamo.BUCKET)) {
            item.put(
                new AttributeUpdates().with(
                    DefaultDynamo.BUCKET,
                    new AttributeValueUpdate()
                        .withAction(AttributeAction.PUT)
                        .withValue(
                            new AttributeValue().withS(
                                item.get(DefaultDynamo.NAME).getS()
                            )
                        )
                )
            );
        }
        return new DefaultDomain(
            item.get(DefaultDynamo.NAME).getS(),
            item.get(DefaultDynamo.KEY).getS(),
            item.get(DefaultDynamo.SECRET).getS(),
            item.get(DefaultDynamo.BUCKET).getS(),
            item.get(DefaultDynamo.REGION).getS(),
            item.get(DefaultDynamo.SYSLOG).getS()
        );
    }

}
