/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Bag of domains.
 *
 * @since 0.0.1
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = "all")
@Loggable(Loggable.DEBUG)
final class Domains extends AbstractSet<Domain> {

    /**
     * List of domains.
     */
    private final transient ConcurrentMap<String, Domain> all =
        new ConcurrentHashMap<>();

    /**
     * Has this domain inside?
     * @param name The domain to check
     * @return TRUE if it is inside already
     */
    public boolean has(@NotNull final String name) {
        return this.all.containsKey(name);
    }

    /**
     * Get domain by name (runtime exception if it doesn't exist).
     * @param name The domain
     * @return Found domain
     */
    public Domain get(@NotNull final String name) {
        final Domain found = this.all.get(name);
        if (found == null) {
            throw new IllegalArgumentException(
                String.format("domain %s not found", name)
            );
        }
        return found;
    }

    @Override
    public boolean add(final Domain domain) {
        boolean added = false;
        if (!this.has(domain.name())) {
            this.all.put(domain.name(), domain);
            added = true;
        }
        return added;
    }

    @Override
    public boolean contains(final Object obj) {
        return this.has(Domain.class.cast(obj).name());
    }

    @Override
    public Iterator<Domain> iterator() {
        return this.all.values().iterator();
    }

    @Override
    public int size() {
        return this.all.size();
    }

}
