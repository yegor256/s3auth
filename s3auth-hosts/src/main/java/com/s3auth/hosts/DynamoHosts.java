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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Collection of hosts, persisted in Amazon DynamoDB.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
@Immutable
@ToString
@EqualsAndHashCode(of = "dynamo")
public final class DynamoHosts implements Hosts {

    /**
     * How often to reload from DynamoDB, in minutes.
     */
    private static final int LIFETIME = 5;

    /**
     * Dynamo DB.
     */
    private final transient Dynamo dynamo;

    /**
     * Default ctor.
     */
    public DynamoHosts() {
        this(new DefaultDynamo());
    }

    /**
     * Default ctor.
     * @param dnm The dynamo abstract
     */
    public DynamoHosts(@NotNull final Dynamo dnm) {
        this.dynamo = dnm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Loggable(Loggable.DEBUG)
    public Host find(
        @NotNull(message = "host name can't be NULL")
        @Pattern(regexp = "[a-zA-Z0-9\\-\\.]+", message = "invalid host name")
        final String name) throws IOException {
        Domain domain = null;
        for (Set<Domain> domains : this.dynamo.load().values()) {
            for (Domain candidate : domains) {
                if (candidate.name().equals(name)) {
                    domain = candidate;
                    break;
                }
            }
            if (domain != null) {
                break;
            }
        }
        if (domain == null) {
            throw new Hosts.NotFoundException(
                String.format(
                    "host '%s' not found, register it at www.s3auth.com",
                    name
                )
            );
        }
        return new SmartHost(new DefaultHost(new DefaultBucket(domain)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    @Loggable(Loggable.DEBUG)
    public Set<Domain> domains(@NotNull @Valid final User user)
        throws IOException {
        // @checkstyle AnonInnerLength (100 lines)
        return new AbstractSet<Domain>() {
            @Override
            public int size() {
                final Iterator<?> iterator = this.iterator();
                int size = 0;
                while (iterator.hasNext()) {
                    iterator.next();
                }
                return size;
            }
            @Override
            public Iterator<Domain> iterator() {
                ConcurrentMap<URN, Set<Domain>> data;
                try {
                    data = DynamoHosts.this.dynamo.load();
                } catch (java.io.IOException ex) {
                    throw new IllegalArgumentException(ex);
                }
                Set<Domain> domains = data.get(user.identity());
                if (domains == null) {
                    domains = new HashSet<Domain>();
                }
                return domains.iterator();
            }
            @Override
            public boolean add(@NotNull @Valid final Domain domain) {
                return DynamoHosts.this.add(user.identity(), domain);
            }
            @Override
            public boolean remove(final Object obj) {
                return DynamoHosts.this.remove(
                    user.identity(), Domain.class.cast(obj)
                );
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void close() throws IOException {
        this.dynamo.close();
    }

    /**
     * Add new domain for the given user (this method is NOT thread-safe).
     * @param user The user
     * @param domain The domain
     * @return Item was added (FALSE means that we already had it)
     */
    private boolean add(final URN user, final Domain domain) {
        try {
            return this.dynamo.add(user, domain);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Remove this domain (this method is NOT thread-safe).
     * @param user Who is removing it
     * @param domain The domain
     * @return Item was removed (FALSE means that we didn't have it)
     */
    private boolean remove(final URN user, final Domain domain) {
        try {
            final ConcurrentMap<URN, Set<Domain>> data = this.dynamo.load();
            if (!data.containsKey(user)) {
                throw new IllegalArgumentException(
                    String.format(
                        "user %s not found, can't delete %s",
                        user,
                        domain.name()
                    )
                );
            }
            if (!data.get(user).contains(domain)) {
                throw new IllegalArgumentException(
                    String.format(
                        "domain %s doesn't belong to %s, can't delete it",
                        domain.name(),
                        user
                    )
                );
            }
            return this.dynamo.remove(domain);
        } catch (java.io.IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

}
