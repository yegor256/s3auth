/**
 * Copyright (c) 2012-2015, s3auth.com
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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link FtpFacade}.
 * @author Felipe Pina (felipe.pina@gmail.com)
 * @version $Id$
 */
public final class FtpFacadeTest {

    /**
     * Connects and disconnects.
     * @throws IOException If it fails inside
     */
    @Test
    @Ignore
    public void connectDisconnect() throws IOException {
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
     * @checkstyle MagicNumberCheck (10 lines)
     */
    private static FtpFacade mockFacade() throws IOException {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            new Answer<Resource>() {
                @Override
                public Resource answer(final InvocationOnMock inv)
                    throws InterruptedException {
                    final Resource answer = Mockito.mock(Resource.class);
                    Mockito.doReturn(new Date(5000L))
                        .when(answer).lastModified();
                    Mockito.doReturn(HttpURLConnection.HTTP_OK)
                        .when(answer).status();
                    return answer;
                }
            }
        ).when(host)
            .fetch(
                Mockito.any(URI.class),
                Mockito.any(Range.class),
                Mockito.any(Version.class)
            );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        return new FtpFacade(hosts, PortMocker.reserve());
    }

}
