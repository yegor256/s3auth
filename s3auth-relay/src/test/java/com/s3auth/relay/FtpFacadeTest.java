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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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
     * Connects and disconnects.
     * @throws IOException If it fails inside
     */
    @Test
    @Disabled
    void connectDisconnect() throws IOException {
        final FtpFacade facade = FtpFacadeTest.mockFacade();
        final FTPClient ftp = new FTPClient();
        try {
            facade.listen();
            ftp.connect("localhost");
            MatcherAssert.assertThat(
                FTPReply.isPositiveCompletion(ftp.getReplyCode()),
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
     * Returns a mock FTPFacade.
     * @return Mock FTPFacade.
     * @throws IOException If something goes wrong.
     */
    private static FtpFacade mockFacade() throws IOException {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                final Resource answer = Mockito.mock(Resource.class);
                Mockito.doReturn(new Date(5000L))
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
        return new FtpFacade(hosts, PortMocker.reserve());
    }

}
