/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.jcabi.aspects.Immutable;
import com.jcabi.urn.URN;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Abstraction on top of DynamoDB SDK.
 *
 * <p>Implementation must be thread-safe.
 *
 * @since 0.0.1
 */
@Immutable
interface Dynamo extends Closeable {

    /**
     * Load all data from DynamoDB.
     * @return Map of users and their domains
     * @throws IOException If some IO problem inside
     */
    Map<URN, Domains> load() throws IOException;

    /**
     * Save to DynamoDB.
     * @param user Who is the owner
     * @param domain The domain to save
     * @return TRUE if successfully added
     * @throws IOException If some IO problem inside
     */
    boolean add(URN user, Domain domain) throws IOException;

    /**
     * Delete from DynamoDB.
     * @param domain The domain to save
     * @return TRUE if successfully deleted
     */
    boolean remove(Domain domain);

    /**
     * Client to Amazon.
     *
     * @since 0.0.1
     */
    @Immutable
    interface Client {
        /**
         * Get Amazon client.
         * @return The client
         */
        AmazonDynamoDB get();
    }

}
