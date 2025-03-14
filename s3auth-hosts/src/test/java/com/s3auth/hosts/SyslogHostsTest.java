/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.productivity.java.syslog4j.server.SyslogServer;
import org.productivity.java.syslog4j.server.SyslogServerEventHandlerIF;
import org.productivity.java.syslog4j.server.SyslogServerEventIF;
import org.productivity.java.syslog4j.server.SyslogServerIF;

/**
 * Test case for {@link SyslogHosts}.
 *
 * @since 0.0.1
 */
final class SyslogHostsTest {

    @Test
    void returnsUnderlyingDomains() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final Domain first = new DomainMocker().init().mock();
        final Domain second = new DomainMocker().init().mock();
        final Set<Domain> domains = new HashSet<>(Arrays.asList(first, second));
        final User user = new UserMocker().init().mock();
        Mockito.doReturn(domains).when(hosts).domains(user);
        @SuppressWarnings("resource")
        final SyslogHosts syslog = new SyslogHosts(hosts);
        MatcherAssert.assertThat(
            syslog.domains(user),
            Matchers.containsInAnyOrder(first, second)
        );
    }

    @Test
    void closesUnderlyingHosts() throws Exception {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final SyslogHosts syslog = new SyslogHosts(hosts);
        syslog.close();
        Mockito.verify(hosts, Mockito.only()).close();
    }

    @Test
    void sendsMessagesToServer() throws Exception {
        final SyslogServerIF server = SyslogServer.getInstance("udp");
        final int port = port();
        server.getConfig().setPort(port);
        final BlockingQueue<String> messages =
            new LinkedBlockingQueue<>();
        server.getConfig().addEventHandler(
            new SyslogServerEventHandlerIF() {
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
                .init()
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
        final int port;
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
