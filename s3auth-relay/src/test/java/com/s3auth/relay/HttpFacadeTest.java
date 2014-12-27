/**
 * Copyright (c) 2012-2014, s3auth.com
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
import com.jcabi.http.Response;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.ResourceMocker;
import com.s3auth.hosts.Version;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.utils.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link HttpFacade}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @checkstyle MultipleStringLiteralsCheck (700 lines)
 * @checkstyle MagicNumberCheck (700 lines)
 * @checkstyle ClassDataAbstractionCoupling (700 lines)
 */
@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports"
})
public final class HttpFacadeTest {

    

    /**
     * HttpFacade can return compressed content with the appropriate request
     * content-encoding and response content-type.
     * @throws Exception If there is some problem inside
     */
	@org.junit.Ignore
    @Test
    public void getsCompressedContent() throws Exception {
        final Host host = Mockito.mock(Host.class);
        final String body = "compressed";
        Mockito.doAnswer(
            new Answer<Resource>() {
                @Override
                public Resource answer(final InvocationOnMock inv) {
                    final Resource answer = new ResourceMocker()
                        .withContent(body).mock();
                    Mockito.doReturn("text/plain")
                        .when(answer).contentType();
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
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        try {
            facade.listen();
            final Response resp =
                new JdkRequest(String.format("http://localhost:%d/", port))
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                    .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        String.format(
                            "Basic %s",
                            Base64.encodeBase64String("a:b".getBytes())
                        )
                    ).uri().path("/a").queryParam("all-versions", "")
                    .back().fetch().as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
            MatcherAssert.assertThat(
                IOUtils.toString(
                    new GZIPInputStream(
                        new ByteArrayInputStream(resp.binary())
                    ),
                    Charsets.UTF_8
                ),
                Matchers.is(body)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can return content thought a secured
     * content-encoding and response content-type.
     * @throws Exception If there is some problem inside
     * @todo #8 For some reason this test is not passing in Travis and Rultor,
     *  even with the jcabi-ssl-maven-plugin. It does work on my local machine.
     *  It fails with javax.net.ssl.SSLHandshakeException with message:
     *  Received fatal alert: handshake_failure. Let's investigate and fix.
     */
    //@org.junit.Ignore
    @Test
    public void getsContentOverSSL() throws Exception {
    	System.out.println("anak " + "Thread.currentThread().getName() " + Thread.currentThread().getName());
    	System.out.println(String.format("anak. java.home=%s, java.version=%s", 
    			System.getProperty("java.home"), 
    			System.getProperty("java.version")));
    	System.out.println(String.format("anak. javax.net.ssl.keyStore=%s\njavax.net.ssl.keyStorePassword=%s\n"
    			+ "javax.net.ssl.trustStore=%s\njavax.net.ssl.trustStorePassword=%s", 
    			System.getProperty("javax.net.ssl.keyStore"),
    			System.getProperty("javax.net.ssl.keyStorePassword"),
    			System.getProperty("javax.net.ssl.trustStore"), 
    			System.getProperty("javax.net.ssl.trustStorePassword")));
    	
    	System.out.println("anak. setting properties");
    	System.setProperty("javax.net.ssl.keyStore", "C:\\work\\odesk\\5 xdsd\\s3auth\\s3auth-relay\\target\\keystore.jks");
		System.setProperty("javax.net.ssl.keyStorePassword", "1d63a34a52d39fe956a94d979e04ef6d");
		System.setProperty("javax.net.ssl.trustStore", "C:\\work\\odesk\\5 xdsd\\s3auth\\s3auth-relay\\target\\cacerts.jks"); 
		System.out.println(String.format("anak. javax.net.ssl.keyStore=%s\njavax.net.ssl.keyStorePassword=%s\n"
    			+ "javax.net.ssl.trustStore=%s\njavax.net.ssl.trustStorePassword=%s", 
    			System.getProperty("javax.net.ssl.keyStore"),
    			System.getProperty("javax.net.ssl.keyStorePassword"),
    			System.getProperty("javax.net.ssl.trustStore"), 
    			System.getProperty("javax.net.ssl.trustStorePassword")));
    			
        final Host host = Mockito.mock(Host.class);
        final String body = "secured";
        final Resource answer = new ResourceMocker().withContent(body).mock();
        Mockito.doReturn(answer).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        final int port = PortMocker.reserve();
        Thread.sleep(100);
        final HttpFacade facade =
            new HttpFacade(hosts, PortMocker.reserve(), port);
        Thread.sleep(100);
        try {
            facade.listen();
            new JdkRequest(String.format("https://localhost:%d/", port))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION, 
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().path("/a")
                .back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(Matchers.is(body));
        } finally {
            facade.close();
        }
    }

}
