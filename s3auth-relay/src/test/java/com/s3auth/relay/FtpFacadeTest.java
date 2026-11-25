/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.ResourceMocker;
import com.s3auth.hosts.Version;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
     * Domain handled by the mocked hosts.
     */
    private static final String DOMAIN = "ftp.example.com";

    /**
     * Content of the resource served by the mocked host.
     */
    private static final String CONTENT = "hello, world!";

    /**
     * FtpFacade can connect successfully.
     *
     * @throws IOException If it fails inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void connectsSuccessfully() throws IOException {
        final int port = PortMocker.reserve();
        final FtpFacade facade =
            FtpFacade.open(FtpFacadeTest.mockHosts(), port);
        final FTPClient ftp = new FTPClient();
        try {
            facade.listen();
            ftp.connect("localhost", port);
            MatcherAssert.assertThat(
                FTPReply.isPositiveCompletion(ftp.getReplyCode()),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                FTPReply.isPositiveCompletion(ftp.quit()),
                Matchers.equalTo(true)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * FtpFacade can authenticate a user and disconnect him.
     *
     * @throws IOException If it fails inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void logsInSuccessfully() throws IOException {
        final int port = PortMocker.reserve();
        final FtpFacade facade =
            FtpFacade.open(FtpFacadeTest.mockHosts(), port);
        final FTPClient ftp = new FTPClient();
        try {
            facade.listen();
            ftp.connect("localhost", port);
            MatcherAssert.assertThat(
                ftp.login(FtpFacadeTest.DOMAIN, "alex:secret"),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                ftp.logout(),
                Matchers.equalTo(true)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * FtpFacade can relay a file to a real FTP client over the data
     * connection, on RETR.
     *
     * @throws IOException If it fails inside
     */
    @Test
    @SuppressWarnings("PMD.UnitTestContainsTooManyAsserts")
    void retrievesFileOverDataConnection() throws IOException {
        final int port = PortMocker.reserve();
        final FtpFacade facade =
            FtpFacade.open(FtpFacadeTest.mockHosts(), port);
        final FTPClient ftp = new FTPClient();
        try {
            facade.listen();
            ftp.connect("localhost", port);
            MatcherAssert.assertThat(
                ftp.login(FtpFacadeTest.DOMAIN, "alex:secret"),
                Matchers.equalTo(true)
            );
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatcherAssert.assertThat(
                ftp.retrieveFile("/index.html", out),
                Matchers.equalTo(true)
            );
            MatcherAssert.assertThat(
                out.toString(StandardCharsets.UTF_8),
                Matchers.equalTo(FtpFacadeTest.CONTENT)
            );
            MatcherAssert.assertThat(
                ftp.logout(),
                Matchers.equalTo(true)
            );
        } finally {
            facade.close();
        }
    }

    private static Hosts mockHosts() throws IOException {
        final Host host = Mockito.mock(Host.class);
        Mockito.doReturn(true)
            .when(host)
            .authorized("alex", "secret");
        Mockito.doAnswer(
            (Answer<Resource>) inv -> new ResourceMocker()
                .init().withContent(FtpFacadeTest.CONTENT).mock()
        ).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(FtpFacadeTest.DOMAIN);
        return hosts;
    }
}
