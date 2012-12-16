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
import com.amazonaws.services.dynamodb.model.DeleteItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;

/**
 * Abstraction on top of DynamoDB SDK.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
final class DefaultDynamo implements Dynamo {

    /**
     * Dynamo DB key, URN of a user.
     */
    private static final String USER = "user.urn";

    /**
     * Dynamo DB key, name of domain.
     */
    private static final String NAME = "domain.name";

    /**
     * Dynamo DB key, AWS key of bucket.
     */
    private static final String KEY = "domain.key";

    /**
     * Dynamo DB key, AWS secret of bucket.
     */
    private static final String SECRET = "domain.secret";

    /**
     * Dynamo DB key, AWS S3 region of bucket.
     */
    private static final String REGION = "domain.region";

    /**
     * AWS client.
     */
    private final transient AmazonDynamoDB client;

    /**
     * Table name.
     */
    private final transient String table;

    /**
     * Public ctor.
     */
    public DefaultDynamo() {
        this(
            DefaultDynamo.live(),
            Manifests.read("S3Auth-AwsDynamoTable")
        );
    }

    /**
     * Ctor for unit tests.
     * @param clnt The client to Dynamo DB
     * @param tbl Table name
     */
    protected DefaultDynamo(final AmazonDynamoDB clnt, final String tbl) {
        this.client = clnt;
        this.table = tbl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.client.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.INFO)
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public ConcurrentMap<URN, Set<Domain>> load() throws IOException {
        final ConcurrentMap<URN, Set<Domain>> domains =
            new ConcurrentHashMap<URN, Set<Domain>>();
        final ScanResult result = this.client.scan(new ScanRequest(this.table));
        for (final Map<String, AttributeValue> item : result.getItems()) {
            if (!item.containsKey(DefaultDynamo.REGION)) {
                item.put(DefaultDynamo.REGION, new AttributeValue("s3"));
            }
            final URN user = URN.create(item.get(DefaultDynamo.USER).getS());
            domains.putIfAbsent(user, new HashSet<Domain>());
            domains.get(user).add(
                new DefaultDomain(
                    item.get(DefaultDynamo.NAME).getS(),
                    item.get(DefaultDynamo.KEY).getS(),
                    item.get(DefaultDynamo.SECRET).getS(),
                    item.get(DefaultDynamo.REGION).getS()
                )
            );
        }
        Logger.info(this, "#load(): %d user(s)", domains.size());
        return domains;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void add(@NotNull final URN user,
        @NotNull final Domain domain) throws IOException {
        final ConcurrentMap<String, AttributeValue> attrs =
            new ConcurrentHashMap<String, AttributeValue>();
        attrs.put(DefaultDynamo.USER, new AttributeValue(user.toString()));
        attrs.put(DefaultDynamo.NAME, new AttributeValue(domain.name()));
        attrs.put(DefaultDynamo.KEY, new AttributeValue(domain.key()));
        attrs.put(DefaultDynamo.SECRET, new AttributeValue(domain.secret()));
        attrs.put(DefaultDynamo.REGION, new AttributeValue(domain.region()));
        final PutItemResult result = this.client.putItem(
            new PutItemRequest(this.table, attrs)
        );
        Logger.info(
            this,
            "#add('%s', '%s'): added, %.2f unit(s)",
            user,
            domain.name(),
            result.getConsumedCapacityUnits()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void remove(@NotNull final Domain domain) throws IOException {
        final DeleteItemResult result = this.client.deleteItem(
            new DeleteItemRequest(
                this.table, new Key(new AttributeValue(domain.name()))
            )
        );
        Logger.info(
            this,
            "#remove('%s'): removed, %.2f unit(s)",
            domain.name(),
            result.getConsumedCapacityUnits()
        );
    }

    /**
     * Make a live client.
     * @return The client to work with
     */
    private static AmazonDynamoDB live() {
        final String key = Manifests.read("S3Auth-AwsDynamoKey");
        final String secret = Manifests.read("S3Auth-AwsDynamoSecret");
        final AmazonDynamoDB client = new AmazonDynamoDBClient(
            new BasicAWSCredentials(key, secret)
        );
        Logger.info(DefaultDynamo.class, "#live(): Dynamo with '%s'", key);
        return client;
    }

}
