/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
        new ConcurrentHashMap<String, Domain>();

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
