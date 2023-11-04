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

import java.util.Set;
import org.hamcrest.CustomMatcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link DynamoHosts}.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DynamoHostsTest {

    /**
     * DynamoHosts can clean cache in runtime.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void updatesCachedData() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().mock();
        final User user = new UserMocker().mock();
        MatcherAssert.assertThat(
            hosts.domains(user).add(domain),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(hosts.domains(user), Matchers.hasSize(1));
        MatcherAssert.assertThat(
            hosts.domains(user).contains(domain),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can reject duplicates.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rejectsDuplicatesFromDifferentUsers() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().withName("ibm.com").mock();
        final User first = new UserMocker()
            .withIdentity("urn:facebook:7743")
            .mock();
        final User second = new UserMocker()
            .withIdentity("urn:facebook:7746")
            .mock();
        hosts.domains(first).remove(domain);
        MatcherAssert.assertThat(
            hosts.domains(first).add(domain),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            hosts.domains(second).add(domain),
            Matchers.is(false)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can protect domains against removal.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void protectsDomainsAgainstRemoval() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker().withName("yahoo.com").mock();
        final User first = new UserMocker()
            .withIdentity("urn:facebook:5543")
            .mock();
        final User second = new UserMocker()
            .withIdentity("urn:facebook:5546")
            .mock();
        hosts.domains(first).remove(domain);
        MatcherAssert.assertThat(
            hosts.domains(first).add(domain),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            hosts.domains(second).remove(domain),
            Matchers.is(false)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can clean/trim domain properties.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void trimsDomainProperties() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain domain = new DomainMocker()
            .withName(" google.com ")
            .withKey(" AKI56FXVOY5FEEZNZXAQ   ")
            .withSecret("  ZFomiC6OObi6gD2J1QQcaW1evMUfqv5fVkpDImI9 ")
            .mock();
        final User first = new UserMocker()
            .withIdentity("urn:facebook:8989")
            .mock();
        hosts.domains(first).remove(domain);
        MatcherAssert.assertThat(
            hosts.domains(first).add(domain),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            hosts.domains(first),
            Matchers.hasItem(
                new CustomMatcher<Domain>("trimmed values") {
                    @Override
                    public boolean matches(final Object obj) {
                        final Domain domain = Domain.class.cast(obj);
                        return "google.com".equals(domain.name())
                            && "AKI56FXVOY5FEEZNZXAQ".equals(domain.key())
                            && domain.secret().startsWith("ZFomiC6OObi");
                    }
                }
            )
        );
        hosts.close();
    }

    /**
     * DynamoHosts can reject invalid user names.
     */
    @Disabled
    @Test
    public void rejectsInvalidUserNames() {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final User user = new UserMocker()
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
    public void rejectsBrokenDomains() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final User user = new UserMocker().mock();
        final Domain[] domains = new Domain[] {
            new DomainMocker().withName("").mock(),
            new DomainMocker().withName("invalid domain name").mock(),
            new DomainMocker().withKey("").mock(),
            new DomainMocker().withSecret("").mock(),
            new DomainMocker().withKey("broken-key").mock(),
            new DomainMocker().withSecret("broken-secret").mock(),
        };
        for (final Domain domain : domains) {
            try {
                hosts.domains(user).add(domain);
                Assert.fail(String.format("exception expected for %s", domain));
            } catch (final javax.validation.ValidationException ex) {
                MatcherAssert.assertThat(ex, Matchers.notNullValue());
            }
        }
        hosts.close();
    }

    /**
     * DynamoHosts can fetch all domains from any user for the super user.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fetchesAllDomainsForSuperUser() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final Domain first = new DomainMocker().withName("first.com").mock();
        final Domain second = new DomainMocker().withName("second.com").mock();
        hosts.domains(
            new UserMocker().withIdentity("urn:facebook:5547").mock()
        ).add(first);
        hosts.domains(
            new UserMocker().withIdentity("urn:facebook:5548").mock()
        ).add(second);
        final Set<Domain> domains = hosts.domains(
            new UserMocker().withIdentity("urn:github:526301").mock()
        );
        MatcherAssert.assertThat(
            domains,
            Matchers.<Domain>iterableWithSize(2)
        );
        hosts.close();
    }

}
