/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.Version;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link FtpFacade}.
 *
 * @since 0.0.1
 */
final class FtpFacadeTest {

    /**
     * Connects successfully.
     *
     * @throws IOException If it fails inside
     */
    @Test
    @Disabled
    void connectsSuccessfully() throws IOException {
        final FtpFacade facade = FtpFacadeTest.mockFacade();
        final FTPClient ftp = new FTPClient();
        try {
            facade.listen();
            ftp.connect("localhost");
            MatcherAssert.assertThat(
                FTPReply.isPositiveCompletion(ftp.getReplyCode()),
                Matchers.equalTo(true)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * Disconnects successfully.
     *
     * @throws IOException If it fails inside
     */
    @Test
    @Disabled
    void disconnectsSuccessfully() throws IOException {
        final FtpFacade facade = FtpFacadeTest.mockFacade();
        final FTPClient ftp = new FTPClient();
        try {
            facade.listen();
            ftp.connect("localhost");
            MatcherAssert.assertThat(
                ftp.logout(),
                Matchers.equalTo(true)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * FtpFacade can accept a TLS connection on the secured port and
     * complete the TLS handshake on it.
     *
     * <p>The FTP worker sends its reply only after the queue of open
     * connections overflows, so the client blocks on reading until the
     * secured socket is accepted and the server side of the TLS channel
     * answers. A successfully decoded FTP reply therefore proves that
     * the secured socket was really accepted and that the TLS handshake
     * completed with the test key material; a plain server socket or a
     * deleted accept schedule would fail this check.</p>
     *
     * @throws IOException If it fails inside
     */
    @Test
    void acceptsConnectionsOnSecuredPort() throws IOException {
        final Hosts hosts = Mockito.mock(Hosts.class);
        final int secured = PortMocker.reserve();
        final FtpFacade facade = FtpFacade.open(
            hosts, PortMocker.reserve(), secured
        );
        facade.listen();
        try {
            MatcherAssert.assertThat(
                "the secured port must accept a TLS connection and reply over it",
                FtpFacadeTest.reply(secured),
                Matchers.startsWith(
                    Integer.toString(FTPReply.SERVICE_NOT_AVAILABLE)
                )
            );
        } finally {
            facade.close();
        }
    }

    /**
     * FtpFacade can release the plain port when binding the secured one fails.
     *
     * @throws IOException If it fails inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void releasesPlainPortWhenSecuredPortBindFails() throws IOException {
        final int plain = PortMocker.reserve();
        final int secured = PortMocker.reserve();
        try (ServerSocket occupied = new ServerSocket(secured)) {
            MatcherAssert.assertThat(
                "the secured port must be occupied before the facade opens",
                occupied.isBound(),
                Matchers.is(true)
            );
            Assertions.assertThrows(
                IOException.class,
                () -> FtpFacade.open(Mockito.mock(Hosts.class), plain, secured)
            );
            try (ServerSocket rebound = new ServerSocket(plain)) {
                MatcherAssert.assertThat(
                    "the plain port must be rebindable after a failed secured bind",
                    rebound.isBound(),
                    Matchers.is(true)
                );
            }
        }
    }

    private static String reply(final int secured) throws IOException {
        try (
            SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault()
                .createSocket()
        ) {
            socket.connect(
                new InetSocketAddress("localhost", secured),
                (int) TimeUnit.SECONDS.toMillis(10L)
            );
            socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(30L));
            try (
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                        socket.getInputStream(), StandardCharsets.UTF_8
                    )
                )
            ) {
                return reader.readLine();
            }
        }
    }

    private static FtpFacade mockFacade() throws IOException {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                final Resource answer = Mockito.mock(Resource.class);
                Mockito.doReturn(Date.from(Instant.ofEpochMilli(5000L)))
                    .when(answer).lastModified();
                Mockito.doReturn(HttpURLConnection.HTTP_OK)
                    .when(answer).status();
                return answer;
            }
        ).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        return FtpFacade.open(hosts, PortMocker.reserve(), PortMocker.reserve());
    }
}
