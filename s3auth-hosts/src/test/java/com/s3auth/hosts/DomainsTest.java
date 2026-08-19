/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link Domains}.
 * @since 0.0.1
 */
final class DomainsTest {

    /**
     * Domains can add and retrieve domains by name.
     */
    @Test
    void addsAndRetrievesDomainByName() {
        final Domain domain = new DomainMocker().init().mock();
        final Domains domains = new Domains();
        domains.add(domain);
        MatcherAssert.assertThat(
            domains.has(domain.name()),
            Matchers.is(true)
        );
    }

    /**
     * Domains can tell whether a domain is contained inside.
     */
    @Test
    void containsAddedDomain() {
        final Domain domain = new DomainMocker().init().mock();
        final Domains domains = new Domains();
        domains.add(domain);
        MatcherAssert.assertThat(
            domains.contains(new DefaultDomain(domain)),
            Matchers.is(true)
        );
    }
}
