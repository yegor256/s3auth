/*
 * Copyright (c) 2012-2025, Yegor Bugayenko
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
