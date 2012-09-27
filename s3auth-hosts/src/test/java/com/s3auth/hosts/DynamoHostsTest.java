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

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.net.URI;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link DynamoHosts}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DynamoHostsTest {

    /**
     * DynamoHosts can load configuration from XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void loadsDynamoConfiguration() throws Exception {
        final Hosts hosts = new DynamoHosts();
        final User user = new UserMocker().mock();
        final Set<Domain> domains = hosts.domains(user);
        final Domain domain = new DomainMocker().mock();
        domains.remove(domain);
        MatcherAssert.assertThat(domains.add(domain), Matchers.is(true));
        MatcherAssert.assertThat(
            hosts.domains(user),
            Matchers.hasSize(Matchers.greaterThan(0))
        );
        final Host host = hosts.find(domain.name());
        MatcherAssert.assertThat(
            host.authorized(domain.key(), domain.secret()),
            Matchers.is(true)
        );
        hosts.close();
    }

    /**
     * DynamoHosts can block calls to .htpasswd.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void blocksFetchingOfSystemResources() throws Exception {
        final Hosts hosts = new DynamoHosts(new DynamoMocker().mock());
        final User user = new UserMocker().mock();
        final Set<Domain> domains = hosts.domains(user);
        final S3Object object = Mockito.mock(S3Object.class);
        Mockito.doReturn(
            new S3ObjectInputStream(IOUtils.toInputStream(""), null)
        ).when(object).getObjectContent();
        final AmazonS3 client = Mockito.mock(AmazonS3.class);
        Mockito.doReturn(object).when(client)
            .getObject(Mockito.any(GetObjectRequest.class));
        final Bucket bucket = new BucketMocker()
            .withName("mocked-bucket")
            .withClient(client).mock();
        MatcherAssert.assertThat(domains.add(bucket), Matchers.is(true));
        final Host host = hosts.find(bucket.name());
        hosts.close();
        host.fetch(URI.create("/.htpasswd"));
    }

    /**
     * DynamoHosts can reject invalid user names.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = javax.validation.ConstraintViolationException.class)
    public void rejectsInvalidUserNames() throws Exception {
        final Hosts hosts = new DynamoHosts();
        final User user = new UserMocker().withIdentity("broken name").mock();
        try {
            hosts.domains(user);
        } finally {
            hosts.close();
        }
    }

    /**
     * DynamoHosts can reject broken domains.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void rejectsBrokenDomains() throws Exception {
        final Hosts hosts = new DynamoHosts();
        final User user = new UserMocker().mock();
        final Domain[] domains = new Domain[] {
            new DomainMocker().withName("").mock(),
            new DomainMocker().withName("invalid domain name").mock(),
            new DomainMocker().withKey("").mock(),
            new DomainMocker().withSecret("").mock(),
            new DomainMocker().withKey("broken-key").mock(),
            new DomainMocker().withSecret("broken-secret").mock(),
        };
        for (Domain domain : domains) {
            try {
                hosts.domains(user).add(domain);
                Assert.fail(String.format("exception expected for %s", domain));
            } catch (javax.validation.ValidationException ex) {
                MatcherAssert.assertThat(ex, Matchers.notNullValue());
            }
        }
        hosts.close();
    }

}
