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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link SyslogHosts}.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 */
public final class SyslogHostsTest {

    /**
     * SyslogHosts can return the domains of its underlying Hosts.
     * @throws Exception If something goes wrong
     */
    @Test
    public void returnsUnderlyingDomains() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final Domain first = new DomainMocker().mock();
        final Domain second = new DomainMocker().mock();
        final Set<Domain> domains = new HashSet<Domain>();
        domains.addAll(Arrays.asList(new Domain[]{first, second}));
        final User user = new UserMocker().mock();
        Mockito.doReturn(domains).when(hosts).domains(user);
        @SuppressWarnings("resource")
        final SyslogHosts syslog = new SyslogHosts(hosts);
        MatcherAssert.assertThat(
            syslog.domains(user),
            Matchers.containsInAnyOrder(first, second)
        );
    }

    /**
     * SyslogHosts can close its underlying Hosts.
     * @throws Exception If something goes wrong
     */
    @Test
    public void closesUnderlyingHosts() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final SyslogHosts syslog = new SyslogHosts(hosts);
        syslog.close();
        Mockito.verify(hosts, Mockito.only()).close();
    }

}
