/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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

import com.jcabi.urn.URN;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mocker of {@link Dynamo}.
 *
 * @since 0.0.1
 */
public final class DynamoMocker {

    /**
     * Users and their domains.
     */
    private final transient ConcurrentMap<URN, Domains> users =
        new ConcurrentHashMap<>();

    /**
     * Mock it.
     * @return The dynamo
     */
    public Dynamo mock() {
        // @checkstyle AnonInnerLength (50 lines)
        return new Dynamo() {
            @Override
            public Map<URN, Domains> load() {
                return Collections.unmodifiableMap(DynamoMocker.this.users);
            }

            @Override
            public boolean add(final URN user, final Domain domain) {
                DynamoMocker.this.users.putIfAbsent(user, new Domains());
                DynamoMocker.this.users.get(user)
                    .add(new DefaultDomain(domain));
                return true;
            }

            @Override
            public boolean remove(final Domain domain) {
                for (final Set<Domain> domains
                    : DynamoMocker.this.users.values()
                ) {
                    domains.remove(domain);
                }
                return true;
            }

            @Override
            public void close() {
                assert this != null;
            }
        };
    }

}
