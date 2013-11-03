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
package com.s3auth.relay;

import com.jcabi.aspects.Parallel;
import com.jcabi.aspects.Tv;
import com.jcabi.log.Logger;
import com.rexsl.test.RestTester;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link HttpFacade}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class HttpFacadeTest {

    /**
     * HttpFacade can process parallel requests.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void handlesParallelHttpRequests() throws Exception {
        final Resource res = new Resource.PlainText("<test/>");
        final Host host = Mockito.mock(Host.class);
        Mockito.doReturn(res).when(host)
            .fetch(Mockito.any(URI.class), Mockito.any(Range.class));
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        final int port = PortMocker.reserve();
        final HttpFacade facade = new HttpFacade(hosts, port);
        facade.listen();
        final URI uri = UriBuilder
            .fromUri(String.format("http://localhost:%d/", port))
            .path("/a").build();
        Logger.debug(this, "sending HTTP requests to %s", uri);
        try {
            HttpFacadeTest.http(uri);
        } finally {
            facade.close();
        }
    }

    /**
     * Make HTTP request.
     * @param path URI to hit
     * @throws Exception If fails
     */
    @Parallel(threads = Tv.TEN)
    private static void http(final URI path) throws Exception {
        final String rnd = RandomStringUtils.randomAlphabetic(Tv.FIVE);
        RestTester.start(UriBuilder.fromUri(path).queryParam("rnd", rnd))
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
            .header(
                HttpHeaders.AUTHORIZATION,
                String.format(
                    "Basic %s",
                    Base64.encodeBase64String("a:b".getBytes())
                )
            )
            .get("read sample page")
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertXPath("/test");
    }

}
