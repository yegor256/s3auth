/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import org.hamcrest.CustomMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DynamoHosts}.
 * @since 0.0.1
 */
final class DynamoHostsTest {

    /**
     * DynamoHosts reports success when a domain is added.
     * @throws Exception If there is some problem inside
     */
    @Test
    void addsDomainReturnsTrue() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        MatcherAssert.assertThat(
            hosts.domains(new UserMocker().init().mock())
                .add(new DomainMocker().init().mock()),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts caches an added domain.
     * @throws Exception If there is some problem inside
     */
    @Test
    void cachesAddedDomain() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init().mock();
        final User user = new UserMocker().init().mock();
        hosts.domains(user).add(domain);
        MatcherAssert.assertThat(hosts.domains(user), Matchers.hasSize(1));
        hosts.close();
    }

    /**
     * DynamoHosts' cached domains contain the domain that was added.
     * @throws Exception If there is some problem inside
     */
    @Test
    void cachedDataContainsAddedDomain() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init().mock();
        final User user = new UserMocker().init().mock();
        hosts.domains(user).add(domain);
        MatcherAssert.assertThat(
            hosts.domains(user).contains(domain),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts allows the first user to add a domain.
     * @throws Exception If there is some problem inside
     */
    @Test
    void addsNewDomainForFirstUser() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init().withName("ibm.com").mock();
        final User first = new UserMocker().init()
            .withIdentity("urn:facebook:7743")
            .mock();
        hosts.domains(first).remove(domain);
        MatcherAssert.assertThat(
            hosts.domains(first).add(domain),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can reject duplicates.
     * @throws Exception If there is some problem inside
     */
    @Test
    void rejectsDuplicateDomainFromAnotherUser() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init().withName("ibm.com").mock();
        final User first = new UserMocker().init()
            .withIdentity("urn:facebook:7743")
            .mock();
        final User second = new UserMocker().init()
            .withIdentity("urn:facebook:7746")
            .mock();
        hosts.domains(first).remove(domain);
        hosts.domains(first).add(domain);
        MatcherAssert.assertThat(
            hosts.domains(second).add(domain),
            Matchers.is(false)
        );
        hosts.close();
    }

    /**
     * DynamoHosts allows the first user to add a domain.
     * @throws Exception If there is some problem inside
     */
    @Test
    void addsDomainForFirstUser() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init().withName("yahoo.com").mock();
        final User first = new UserMocker().init()
            .withIdentity("urn:facebook:5543")
            .mock();
        hosts.domains(first).remove(domain);
        MatcherAssert.assertThat(
            hosts.domains(first).add(domain),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can protect domains against removal.
     * @throws Exception If there is some problem inside
     */
    @Test
    void protectsDomainsAgainstRemovalByAnotherUser() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init().withName("yahoo.com").mock();
        final User first = new UserMocker().init()
            .withIdentity("urn:facebook:5543")
            .mock();
        final User second = new UserMocker().init()
            .withIdentity("urn:facebook:5546")
            .mock();
        hosts.domains(first).remove(domain);
        hosts.domains(first).add(domain);
        MatcherAssert.assertThat(
            hosts.domains(second).remove(domain),
            Matchers.is(false)
        );
        hosts.close();
    }

    /**
     * DynamoHosts allows adding a domain with untrimmed properties.
     * @throws Exception If there is some problem inside
     */
    @Test
    void addsDomainWithUntrimmedProperties() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init()
            .withName(" google.com ")
            .withKey(" AKI56FXVOY5FEEZNZXAQ   ")
            .withSecret("  ZFomiC6OObi6gD2J1QQcaW1evMUfqv5fVkpDImI9 ")
            .mock();
        final User first = new UserMocker().init()
            .withIdentity("urn:facebook:8989")
            .mock();
        hosts.domains(first).remove(domain);
        MatcherAssert.assertThat(
            hosts.domains(first).add(domain),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can clean/trim domain properties.
     * @throws Exception If there is some problem inside
     */
    @Test
    void trimsDomainPropertiesOnAdd() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().init()
            .withName(" google.com ")
            .withKey(" AKI56FXVOY5FEEZNZXAQ   ")
            .withSecret("  ZFomiC6OObi6gD2J1QQcaW1evMUfqv5fVkpDImI9 ")
            .mock();
        final User first = new UserMocker().init()
            .withIdentity("urn:facebook:8989")
            .mock();
        hosts.domains(first).remove(domain);
        hosts.domains(first).add(domain);
        MatcherAssert.assertThat(
            hosts.domains(first),
            Matchers.hasItem(
                new CustomMatcher<Domain>("trimmed values") {
                    @Override
                    public boolean matches(final Object obj) {
                        final Domain found = Domain.class.cast(obj);
                        return "google.com".equals(found.name())
                            && "AKI56FXVOY5FEEZNZXAQ".equals(found.key())
                            && found.secret().startsWith("ZFomiC6OObi");
                    }
                }
            )
        );
        hosts.close();
    }

    /**
     * DynamoHosts can reject invalid user names.
     */
    @Test
    @Disabled
    void rejectsInvalidUserNames() {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final User user = new UserMocker().init()
            .withIdentity("urn:unknown:4254353")
            .mock();
        Assertions.assertThrows(
            javax.validation.ConstraintViolationException.class,
            () -> {
                try {
                    hosts.domains(user);
                } finally {
                    hosts.close();
                }
            }
        );
    }

    /**
     * DynamoHosts can reject broken domains.
     * @throws Exception If there is some problem inside
     */
    @Test
    @Disabled
    void rejectsBrokenDomains() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final User user = new UserMocker().init().mock();
        final Domain[] domains = {
            new DomainMocker().init().withName("").mock(),
            new DomainMocker().init().withName("invalid domain name").mock(),
            new DomainMocker().init().withKey("").mock(),
            new DomainMocker().init().withSecret("").mock(),
            new DomainMocker().init().withKey("broken-key").mock(),
            new DomainMocker().init().withSecret("broken-secret").mock(),
        };
        for (final Domain domain : domains) {
            Assertions.assertThrows(
                javax.validation.ValidationException.class,
                () -> hosts.domains(user).add(domain)
            );
        }
        hosts.close();
    }

    /**
     * DynamoHosts can fetch all domains from any user for the super user.
     * @throws Exception If there is some problem inside
     */
    @Test
    void fetchesAllDomainsForSuperUser() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain first = new DomainMocker().init().withName("first.com").mock();
        final Domain second = new DomainMocker().init().withName("second.com").mock();
        hosts.domains(
            new UserMocker().init().withIdentity("urn:facebook:5547").mock()
        ).add(first);
        hosts.domains(
            new UserMocker().init().withIdentity("urn:facebook:5548").mock()
        ).add(second);
        MatcherAssert.assertThat(
            hosts.domains(
                new UserMocker().init().withIdentity("urn:github:526301").mock()
            ),
            Matchers.iterableWithSize(2)
        );
        hosts.close();
    }
}
