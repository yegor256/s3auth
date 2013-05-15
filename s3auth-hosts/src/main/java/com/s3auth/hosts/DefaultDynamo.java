/**
 * Copyright (c) 2012, s3auth.com
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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDB;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.DeleteItemRequest;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import java.io.IOException;
import java.util.Map;
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
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "client", "table" })
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
     * Dynamo DB key, AWS S3 region of bucket.
     */
    public static final String REGION = "domain.region";

    /**
     * Lifetime of registry in memory, in minutes.
     */
    private static final int LIFETIME = 5;

    /**
     * Client.
     */
    private final transient Dynamo.Client client;

    /**
     * Table name.
     */
    private final transient String table;

    /**
     * Public ctor.
     */
    public DefaultDynamo() {
        this(
            new Dynamo.Client() {
                @Override
                public AmazonDynamoDB get() {
                    return new AmazonDynamoDBClient(
                        new BasicAWSCredentials(
                            Manifests.read("S3Auth-AwsDynamoKey"),
                            Manifests.read("S3Auth-AwsDynamoSecret")
                        )
                    );
                }
            },
            Manifests.read("S3Auth-AwsDynamoTable")
        );
    }

    /**
     * Ctor for unit tests.
     * @param clnt The client to Dynamo DB
     * @param tbl Table name
     */
    protected DefaultDynamo(@NotNull final Dynamo.Client clnt,
        @NotNull final String tbl) {
        this.client = clnt;
        this.table = tbl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    @Cacheable(lifetime = DefaultDynamo.LIFETIME, unit = TimeUnit.MINUTES)
    public ConcurrentMap<URN, Domains> load() throws IOException {
        final ConcurrentMap<URN, Domains> domains =
            new ConcurrentHashMap<URN, Domains>();
        final AmazonDynamoDB amazon = this.client.get();
        final ScanResult result = amazon.scan(new ScanRequest(this.table));
        for (final Map<String, AttributeValue> item : result.getItems()) {
            final URN user = URN.create(item.get(DefaultDynamo.USER).getS());
            domains.putIfAbsent(user, new Domains());
            domains.get(user).add(
                new DefaultDomain(
                    item.get(DefaultDynamo.NAME).getS(),
                    item.get(DefaultDynamo.KEY).getS(),
                    item.get(DefaultDynamo.SECRET).getS(),
                    item.get(DefaultDynamo.REGION).getS()
                )
            );
        }
        amazon.shutdown();
        return domains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushBefore
    public boolean add(@NotNull final URN user,
        @NotNull final Domain domain) throws IOException {
        final ConcurrentMap<String, AttributeValue> attrs =
            new ConcurrentHashMap<String, AttributeValue>();
        attrs.put(DefaultDynamo.USER, new AttributeValue(user.toString()));
        attrs.put(DefaultDynamo.NAME, new AttributeValue(domain.name()));
        attrs.put(DefaultDynamo.KEY, new AttributeValue(domain.key()));
        attrs.put(DefaultDynamo.SECRET, new AttributeValue(domain.secret()));
        attrs.put(DefaultDynamo.REGION, new AttributeValue(domain.region()));
        final AmazonDynamoDB amazon = this.client.get();
        amazon.putItem(new PutItemRequest(this.table, attrs));
        amazon.shutdown();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable.FlushBefore
    public boolean remove(@NotNull final Domain domain) throws IOException {
        final AmazonDynamoDB amazon = this.client.get();
        amazon.deleteItem(
            new DeleteItemRequest(
                this.table, new Key(new AttributeValue(domain.name()))
            )
        );
        amazon.shutdown();
        return true;
    }

}
