/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Parallel;
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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.utils.DateUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

/**
 * Test case for {@link HttpFacade}.
 *
 * @since 0.0.1
 */
@SuppressWarnings({
    "PMD.AvoidDuplicateLiterals",
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports"
})
final class HttpFacadeTest {

    /**
     * HttpFacade can process parallel requests.
     * @throws Exception If there is some problem inside
     */
    @Test
    void handlesParallelHttpRequests() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                TimeUnit.SECONDS.sleep(1L);
                throw new IllegalStateException("hello, world!");
            }
        ).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        facade.listen();
        final URI uri = UriBuilder
            .fromUri(String.format("http://localhost:%d/", port))
            .path("/a").build();
        try {
            HttpFacadeTest.http(uri);
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can process the If-Modified-Since header.
     * @throws Exception If there is some problem inside
     */
    @Test
    void handlesIfModifiedSinceHeader() throws Exception {
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
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        facade.listen();
        final URI uri = UriBuilder
            .fromUri(String.format("http://localhost:%d/", port))
            .path("/a").build();
        try {
            new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                )
                .header(
                    HttpHeaders.IF_MODIFIED_SINCE,
                    DateUtils.formatDate(new Date(2000L))
                ).uri().back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                )
                .header(
                    HttpHeaders.IF_MODIFIED_SINCE,
                    DateUtils.formatDate(new Date(10_000L))
                ).uri().back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_MODIFIED);
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade returns the Last-Modified header with the response.
     * @throws Exception If there is some problem inside
     */
    @Test
    void respondsWithLastModifiedHeader() throws Exception {
        final Date date = new Date();
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                final Resource answer = Mockito.mock(Resource.class);
                Mockito.doReturn(date)
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
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        try {
            facade.listen();
            final URI uri = UriBuilder
                .fromUri(String.format("http://localhost:%d/", port))
                .path("/a").build();
            final Response resp = new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            MatcherAssert.assertThat(
                resp.headers().get(HttpHeaders.LAST_MODIFIED).get(0),
                Matchers.is(DateUtils.formatDate(date))
            );
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade returns the Age header with the response.
     * @throws Exception If there is some problem inside
     */
    @Test
    void respondsWithAgeHeader() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                final Resource answer = Mockito.mock(Resource.class);
                Mockito.doReturn(HttpURLConnection.HTTP_OK)
                    .when(answer).status();
                Thread.sleep(1100L);
                return answer;
            }
        ).when(host).fetch(
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
            final URI uri = UriBuilder
                .fromUri(String.format("http://localhost:%d/", port))
                .path("/a").build();
            final Response resp = new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            MatcherAssert.assertThat(
                Integer.parseInt(resp.headers().get("Age").get(0)),
                Matchers.greaterThanOrEqualTo(1)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can parse S3 version query and pass it on to Resource.
     * @throws Exception If there is some problem inside
     */
    @Test
    void canParseVersionQuery() throws Exception {
        final String version = "1234";
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                MatcherAssert.assertThat(
                    ((Version) inv.getArguments()[2]).version(),
                    Matchers.is(version)
                );
                final Resource answer = Mockito.mock(Resource.class);
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
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        try {
            facade.listen();
            final URI uri = UriBuilder
                .fromUri(String.format("http://localhost:%d/", port))
                .path("/a").queryParam("ver", version).build();
            new JdkRequest(uri).header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade will request the latest version if it is not specified.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsLatestVersion() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                MatcherAssert.assertThat(
                    (Version) inv.getArguments()[2],
                    Matchers.is(Version.LATEST)
                );
                final Resource answer = Mockito.mock(Resource.class);
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
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        try {
            facade.listen();
            final URI uri = UriBuilder
                .fromUri(String.format("http://localhost:%d/", port))
                .path("/a").build();
            new JdkRequest(uri).header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can request the list of versions of an object.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsVersionListing() throws Exception {
        final Host host = Mockito.mock(Host.class);
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                MatcherAssert.assertThat(
                    (Version) inv.getArguments()[2],
                    Matchers.is(Version.LIST)
                );
                final Resource answer = Mockito.mock(Resource.class);
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
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, port, PortMocker.reserve());
        try {
            facade.listen();
            new JdkRequest(String.format("http://localhost:%d/", port))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().path("/a").queryParam("all-versions", "")
                .back().fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can return compressed content with the appropriate request
     * content-encoding and response content-type.
     * @throws Exception If there is some problem inside
     */
    @Test
    void getsCompressedContent() throws Exception {
        final Host host = Mockito.mock(Host.class);
        final String body = "compressed";
        Mockito.doAnswer(
            (Answer<Resource>) inv -> {
                final Resource answer = Mockito.spy(
                    new ResourceMocker().init()
                        .withContent(body)
                        .withHeaders("gzip")
                        .mock()
                );
                Mockito.doReturn("text/plain")
                    .when(answer).contentType();
                return answer;
            }
        ).when(host).fetch(
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
                    StandardCharsets.UTF_8
                ),
                Matchers.is(body)
            );
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can return content thought a secured content-encoding and
     * response content-type.
     * @throws Exception If there is some problem inside
     * @todo #191:30mins The test fails to retrieve the expected content over
     *  SSL. The response body is empty while it should be "secured".
     *  Test fails on line 570.
     *  Let's fix it and unignore the test.
     */
    @Test
    @Disabled
    void getsContentOverSsl() throws Exception {
        MatcherAssert.assertThat(
            System.getProperty("javax.net.ssl.keyStore"),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            System.getProperty("javax.net.ssl.keyStorePassword"),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            System.getProperty("javax.net.ssl.trustStore"),
            Matchers.notNullValue()
        );
        MatcherAssert.assertThat(
            System.getProperty("javax.net.ssl.trustStorePassword"),
            Matchers.notNullValue()
        );
        final Host host = Mockito.mock(Host.class);
        final String body = "secured";
        final Resource answer = new ResourceMocker().init().withContent(body).mock();
        Mockito.doReturn(answer).when(host).fetch(
            Mockito.any(URI.class),
            Mockito.any(Range.class),
            Mockito.any(Version.class)
        );
        final Hosts hosts = Mockito.mock(Hosts.class);
        Mockito.doReturn(host).when(hosts).find(Mockito.anyString());
        final int port = PortMocker.reserve();
        final HttpFacade facade =
            new HttpFacade(hosts, PortMocker.reserve(), port);
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

    /**
     * HttpFacade closes the Resource after fetching data.
     * @throws Exception If there is some problem inside
     */
    @Test
    void closesUnderlyingResource() throws Exception {
        final Host host = Mockito.mock(Host.class);
        final Resource resource = Mockito.spy(new ResourceMocker().init().mock());
        Mockito.doReturn(resource).when(host).fetch(
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
            final URI uri = UriBuilder
                .fromUri(String.format("http://localhost:%d/", port))
                .path("/a").build();
            new JdkRequest(uri)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                .header(
                    HttpHeaders.AUTHORIZATION,
                    String.format(
                        "Basic %s",
                        Base64.encodeBase64String("a:b".getBytes())
                    )
                ).uri().back().fetch();
            TimeUnit.SECONDS.sleep(3);
            Mockito.verify(resource, Mockito.times(1)).close();
        } finally {
            facade.close();
        }
    }

    /**
     * HttpFacade can service HTTP HEAD methods.
     * @throws Exception If there is some problem inside
     */
    @Test
    void servicesHeadMethod() throws Exception {
        final Host host = Mockito.mock(Host.class);
        final Resource resource = Mockito.spy(
            new ResourceMocker().init()
                .withContent("should not appear in body")
                .mock()
        );
        Mockito.doReturn(resource).when(host).fetch(
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
            final URI uri = UriBuilder
                .fromUri(String.format("http://localhost:%d/", port))
                .path("/a").build();
            MatcherAssert.assertThat(
                new ApacheRequest(uri)
                    .method("HEAD")
                    .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
                    .header(
                        HttpHeaders.AUTHORIZATION,
                        String.format(
                            "Basic %s",
                            Base64.encodeBase64String("a:b".getBytes())
                        )
                    ).uri().back().fetch().body(),
                Matchers.is("")
            );
            Mockito.verify(resource, Mockito.never())
                .writeTo(Mockito.any(OutputStream.class));
        } finally {
            facade.close();
        }
    }

    /**
     * Make HTTP request.
     * @param path URI to hit
     * @throws Exception If fails
     */
    @Parallel(threads = 50)
    private static void http(final URI path) throws Exception {
        new JdkRequest(path)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
            .header(
                HttpHeaders.AUTHORIZATION,
                String.format(
                    "Basic %s",
                    Base64.encodeBase64String("a:b".getBytes())
                )
            )
            .uri()
            .queryParam("rnd", RandomStringUtils.randomAlphabetic(5))
            .back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_INTERNAL_ERROR)
            .assertBody(Matchers.containsString("hello"));
    }

}
