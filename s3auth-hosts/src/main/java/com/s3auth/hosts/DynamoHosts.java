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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.urn.URN;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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
 * @since 0.0.1
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.UseConcurrentHashMap" })
@Immutable
@ToString
@EqualsAndHashCode(of = "dynamo")
@Loggable(Loggable.DEBUG)
public final class DynamoHosts implements Hosts {

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

    @Override
    @NotNull
    @Loggable(value = Loggable.DEBUG, ignore = Hosts.NotFoundException.class)
    public Host find(
        @NotNull(message = "host name can't be NULL")
        @Pattern(
            regexp = "[a-zA-Z0-9\\-\\.]+",
            message = "hostname contains characters not allowed by RFC1123"
        )
        final String name) throws IOException {
        final Domain domain = this.byName(name);
        if (domain == null) {
            throw new Hosts.NotFoundException(
                String.format(
                    // @checkstyle LineLength (1 line)
                    "host '%s' not found, register it at www.s3auth.com and wait for 10 minutes",
                    name
                )
            );
        }
        return new RejectingHost(
            new FastHost(
                new SmartHost(
                    new DefaultHost(new DefaultBucket(domain))
                )
            ),
            "/wp-content/uploads/images/.*"
        );
    }

    @Override
    @NotNull
    public Set<Domain> domains(@NotNull @Valid final User user)
        throws IOException {
        final Map<URN, Domains> data = this.dynamo.load();
        Domains domains;
        try {
            if (user.identity().equals(new URN("urn:github:526301"))) {
                domains = new Domains();
                for (final Domains dmns : data.values()) {
                    domains.addAll(dmns);
                }
            } else {
                domains = data.get(user.identity());
            }
            if (domains == null) {
                domains = new Domains();
            }
        } catch (final URISyntaxException ex) {
            throw new IOException(ex);
        }
        return new DynamoHosts.Wrap(user, domains);
    }

    @Override
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
        boolean added = false;
        try {
            if (this.byName(domain.name()) == null) {
                added = this.dynamo.add(user, new DefaultDomain(domain));
            }
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        return added;
    }

    /**
     * Remove this domain (this method is NOT thread-safe).
     * @param user Who is removing it
     * @param domain The domain
     * @return Item was removed (FALSE means that we didn't have it)
     */
    private boolean remove(final URN user, final Domain domain) {
        boolean removed = false;
        try {
            final Map<URN, Domains> data = this.dynamo.load();
            if (data.containsKey(user) && data.get(user).contains(domain)) {
                removed = this.dynamo.remove(domain);
            }
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
        return removed;
    }

    /**
     * Find domain by name or NULL if not found.
     * @param name Name of domain to find
     * @return Domain found or null
     * @throws IOException If something goes wrong
     */
    private Domain byName(final String name) throws IOException {
        Domain domain = null;
        for (final Set<Domain> domains : this.dynamo.load().values()) {
            for (final Domain candidate : domains) {
                if (candidate.name().equals(name)) {
                    domain = candidate;
                    break;
                }
            }
            if (domain != null) {
                break;
            }
        }
        return domain;
    }

    /**
     * Wrap of domains.
     *
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private final class Wrap extends AbstractSet<Domain> {
        /**
         * User.
         */
        private final transient User user;

        /**
         * Domains.
         */
        private final transient Domains domains;

        /**
         * Public ctor.
         * @param usr User
         * @param dmns Domains
         */
        Wrap(final User usr, final Domains dmns) {
            super();
            this.user = usr;
            this.domains = dmns;
        }

        @Override
        public int size() {
            return this.domains.size();
        }

        @Override
        public Iterator<Domain> iterator() {
            return this.domains.iterator();
        }

        @Override
        public boolean contains(final Object obj) {
            return this.domains.contains(obj);
        }

        @Override
        public boolean add(@NotNull @Valid final Domain domain) {
            return DynamoHosts.this.add(this.user.identity(), domain);
        }

        @Override
        public boolean remove(final Object obj) {
            return DynamoHosts.this.remove(
                this.user.identity(), Domain.class.cast(obj)
            );
        }
    }

}
