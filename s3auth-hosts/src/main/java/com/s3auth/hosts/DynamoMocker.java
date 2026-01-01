/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
