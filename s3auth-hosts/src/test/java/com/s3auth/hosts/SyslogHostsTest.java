/**
 * Copyright (c) 2012-2019, s3auth.com
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

import java.net.DatagramSocket;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;

/**
 * Test case for {@link SyslogHosts}.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @checkstyle MultipleStringLiterals (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
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

    /**
     * SyslogHosts can send messages to the syslog server.
     * @throws Exception If something goes wrong
     */
    @Test
    public void sendsMessagesToServer() throws Exception {
        final SyslogServerIF server = SyslogServer.getInstance("udp");
        final int port = port();
        server.getConfig().setPort(port);
        final BlockingQueue<String> messages =
            new LinkedBlockingQueue<String>();
        server.getConfig().addEventHandler(
            new SyslogServerEventHandlerIF() {
                /**
                 * Serial Version UID.
                 */
                private static final long serialVersionUID =
                    -7043255643054178133L;
                @Override
                public void event(final SyslogServerIF syslg,
                    final SyslogServerEventIF event) {
                    messages.add(event.getMessage());
                }
            }
        );
        final ExecutorService exec = Executors.newSingleThreadExecutor();
        try {
            exec.execute(server);
            final Hosts hosts = Mockito.mock(Hosts.class);
            final String path = "/path/to/fetch";
            final Host host = new HostMocker()
                .withContent(URI.create(path), "blah")
                .withSyslog(String.format("localhost:%d", port))
                .mock();
            Mockito.doReturn(host).when(hosts)
                .find(Mockito.anyString());
            ResourceMocker.toByteArray(
                new SyslogHosts(hosts).find("dummy")
                    .fetch(URI.create(path), Range.ENTIRE, Version.LATEST)
            );
            MatcherAssert.assertThat(
                messages.poll(1, TimeUnit.MINUTES),
                Matchers.containsString(String.format("from %s", path))
            );
        } finally {
            server.shutdown();
            exec.shutdown();
        }
    }

    /**
     * Find and return the first available port.
     * @return The port number
     */
    private static int port() {
        int port;
        try {
            final DatagramSocket socket = new DatagramSocket(0);
            try {
                port = socket.getLocalPort();
            } finally {
                socket.close();
            }
        } catch (final java.io.IOException ex) {
            throw new IllegalStateException("Failed to reserve port", ex);
        }
        return port;
    }
}
