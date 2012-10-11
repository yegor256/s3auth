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

import com.jcabi.log.Logger;
import java.io.IOException;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicLong;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Collection of hosts, persisted in Amazon DynamoDB.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class DynamoHosts implements Hosts {

    /**
     * How often to reload from DynamoDB, in milliseconds.
     */
    private static final int PERIOD_MS = 1 * 60 * 1000;

    /**
     * When recent update happened?
     */
    private final transient AtomicLong updated = new AtomicLong();

    /**
     * Dynamo DB.
     */
    private final transient Dynamo dynamo;

    /**
     * Domains with their hosts.
     */
    private final transient ConcurrentMap<String, Host> hosts =
        new ConcurrentHashMap<String, Host>();

    /**
     * Users with their domains.
     */
    private final transient ConcurrentMap<String, Set<Domain>> users =
        new ConcurrentHashMap<String, Set<Domain>>();

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
    public Host find(
        @NotNull(message = "host name can't be NULL")
        @Pattern(regexp = "[a-zA-Z0-9\\-\\.]+", message = "invalid host name")
        final String name) throws IOException {
        this.update();
        final Host host = this.hosts.get(name);
        if (host == null) {
            throw new Hosts.NotFoundException(
                String.format(
                    "host '%s' not found, register it at www.s3auth.com",
                    name
                )
            );
        }
        Logger.debug(this, "#find('%s'): found %[type]s", name, host);
        return host;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Domain> domains(
        @NotNull @Valid final User user) throws IOException {
        this.update();
        this.users.putIfAbsent(
            user.identity(),
            new CopyOnWriteArraySet<Domain>()
        );
        final Set<Domain> set = this.users.get(user.identity());
        // @checkstyle AnonInnerLength (100 lines)
        return new AbstractSet<Domain>() {
            @Override
            public int size() {
                return set.size();
            }
            @Override
            public Iterator<Domain> iterator() {
                return set.iterator();
            }
            @Override
            public boolean add(@NotNull @Valid final Domain domain) {
                boolean added = false;
                final Domain normal = DynamoHosts.normalize(domain);
                if (DynamoHosts.this.add(user.identity(), normal)) {
                    set.add(normal);
                    added = true;
                }
                return added;
            }
            @Override
            public boolean remove(final Object obj) {
                boolean removed = false;
                final Domain domain = DynamoHosts.normalize(
                    Domain.class.cast(obj)
                );
                if (DynamoHosts.this.remove(user.identity(), domain)) {
                    set.remove(domain);
                    removed = true;
                }
                return removed;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.dynamo.close();
        for (Host host : this.hosts.values()) {
            host.close();
        }
    }

    /**
     * Refresh content from Dynamo DB into local variables.
     * @throws IOException If some IO problem inside
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void update() throws IOException {
        synchronized (this.updated) {
            if (System.currentTimeMillis() - this.updated.get()
                > DynamoHosts.PERIOD_MS) {
                this.users.clear();
                this.users.putAll(this.dynamo.load());
                for (String user : this.users.keySet()) {
                    for (Domain domain : this.users.get(user)) {
                        this.add(user, domain);
                    }
                }
                Logger.info(
                    this,
                    "#update(): %d host(s), %d user(s) loaded",
                    this.hosts.size(),
                    this.users.size()
                );
                this.updated.set(System.currentTimeMillis());
            }
        }
    }

    /**
     * Normalize domain, into a common type.
     * @param domain The domain to normalize
     * @return Normalized domain
     */
    private static Bucket normalize(final Domain domain) {
        Bucket normalized;
        if (domain instanceof Bucket) {
            normalized = Bucket.class.cast(domain);
        } else if (domain instanceof DefaultDomain) {
            normalized = new DefaultBucket(domain);
        } else {
            normalized = new DefaultBucket(new DefaultDomain(domain));
        }
        return normalized;
    }

    /**
     * Add new domain for the given user (this method is NOT thread-safe).
     * @param user The user
     * @param domain The domain
     * @return Item was added (FALSE means that we already had it)
     */
    private boolean add(final String user, final Domain domain) {
        boolean added;
        if (this.hosts.containsKey(domain.name())) {
            Logger.warn(this, "#add('%s', '%s'): no need", user, domain);
            added = false;
        } else {
            try {
                this.dynamo.add(user, domain);
            } catch (java.io.IOException ex) {
                throw new IllegalArgumentException(ex);
            }
            this.hosts.put(
                domain.name(),
                new SmartHost(new DefaultHost(DynamoHosts.normalize(domain)))
            );
            added = true;
        }
        return added;
    }

    /**
     * Remove this domain (this method is NOT thread-safe).
     * @param user Who is removing it
     * @param domain The domain
     * @return Item was removed (FALSE means that we didn't have it)
     */
    private boolean remove(final String user, final Domain domain) {
        boolean removed;
        if (this.hosts.containsKey(domain.name())
            && this.users.get(user).contains(domain)) {
            try {
                this.dynamo.remove(domain);
            } catch (java.io.IOException ex) {
                throw new IllegalArgumentException(ex);
            }
            this.hosts.remove(domain.name());
            removed = true;
        } else {
            Logger.warn(
                this,
                "#remove('%s', '%s'/%[type]s): no need",
                user,
                domain,
                domain
            );
            removed = false;
        }
        return removed;
    }

}
