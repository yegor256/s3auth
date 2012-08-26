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
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

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
    private final transient Dynamo dynamo = new Dynamo();

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
     * {@inheritDoc}
     */
    @Override
    public Host find(final String name) throws IOException {
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
        Logger.debug(this, "#find('%s'): found", name);
        return host;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Domain> domains(final User user) throws IOException {
        this.update();
        this.users.putIfAbsent(
            user.identity(),
            new ConcurrentSkipListSet<Domain>()
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
            public boolean add(final Domain domain) {
                boolean added;
                if (DynamoHosts.this.hosts.containsKey(domain.name())) {
                    added = false;
                } else {
                    try {
                        DynamoHosts.this.dynamo.add(user.identity(), domain);
                    } catch (java.io.IOException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    set.add(new DefaultDomain(domain));
                    DynamoHosts.this.hosts.put(
                        domain.name(),
                        new DefaultHost(domain)
                    );
                    added = true;
                }
                return added;
            }
            @Override
            public boolean remove(final Object obj) {
                boolean removed;
                final Domain domain = Domain.class.cast(obj);
                if (DynamoHosts.this.hosts.containsKey(domain.name())) {
                    try {
                        DynamoHosts.this.dynamo.remove(domain);
                    } catch (java.io.IOException ex) {
                        throw new IllegalArgumentException(ex);
                    }
                    DynamoHosts.this.hosts.remove(domain.name());
                    set.remove(new DefaultDomain(domain));
                    removed = true;
                } else {
                    removed = false;
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
     * Refresh content from Dynamo DB.
     * @throws IOException If some IO problem inside
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private void update() throws IOException {
        synchronized (this.updated) {
            if (System.currentTimeMillis() - this.updated.get()
                > DynamoHosts.PERIOD_MS) {
                this.users.clear();
                this.users.putAll(this.dynamo.load());
                for (ConcurrentMap.Entry<String, Set<Domain>> entry
                    : this.users.entrySet()) {
                    for (Domain domain : entry.getValue()) {
                        this.hosts.put(domain.name(), new DefaultHost(domain));
                    }
                }
                Logger.debug(
                    this,
                    "#update(): %d host(s) loaded",
                    this.hosts.size()
                );
                this.updated.set(System.currentTimeMillis());
            }
        }
    }

}
