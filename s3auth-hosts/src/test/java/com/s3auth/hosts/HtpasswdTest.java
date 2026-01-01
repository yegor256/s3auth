/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.s3auth.hosts.Host.CloudWatch;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Htpasswd}.
 * @since 0.0.1
 */
final class HtpasswdTest {

    @Test
    void showsStatsInToString() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(this.host("peter-pen:hello")),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    @Test
    void showsStatsInToStringWithIoException() {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doThrow(new AmazonClientException("")).when(aws)
            .getObject(Mockito.any(GetObjectRequest.class));
        MatcherAssert.assertThat(
            new Htpasswd(
                new DefaultHost(
                    new BucketMocker().init().withClient(aws).mock(),
                    this.cloudWatch()
                )
            ),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * Htpasswd can manage apache hashes, with MD5 algorithm.
     * @throws Exception If there is some problem inside
     * @link ftp://ftp.arlut.utexas.edu/pub/java_hashes/MD5Crypt.java
     */
    @Test
    void understandsApacheNativeHashValues() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("foo:$apr1$1/yqU0TM$fx36ZuZIapXW39ivIA5AR.")
            ).authorized("foo", "test"),
            Matchers.is(true)
        );
    }

    @Test
    void understandsShaHashValues() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("john:{SHA}6qagQQ8seo0bw69C/mNKhYbSf34=")
            ).authorized("john", "victory"),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("susi:{SHA}05jkyU4N/+ADjjOghbccdO5zKHE=")
            ).authorized("susi", "a7a6s-"),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("william:{SHA}qUqP5cyxm6YcTAhz05Hph5gvu9M=")
            ).authorized("william", "invalid-pwd"),
            Matchers.is(false)
        );
    }

    /**
     * Htpasswd can manage apache hashes, with PLAIN/TEXT algorithm.
     * @throws Exception If there is some problem inside
     */
    @Test
    void understandsPlainTextHashValues() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("erik:super-secret-password-\u0433")
            ).authorized("erik", "super-secret-password-\u0433"),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("nick:secret-password-\u0433")
            ).authorized("nick", "incorrect-password"),
            Matchers.is(false)
        );
    }

    /**
     * Htpasswd can manage apache hashes, with Crypt algorithm.
     * @throws Exception If there is some problem inside
     * @link <a href="http://jxutil.sourceforge.net/API/org/sourceforge/jxutil/JCrypt.html">JCrypt</a>
     * @link <a href="http://www.dynamic.net.au/christos/crypt/">Crypt</a>
     */
    @Test
    void understandsCryptHashValues() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("alex:QS3Wb6MddltY2")
            ).authorized("alex", "fire"),
            Matchers.is(true)
        );
    }

    /**
     * Htpasswd can ignore broken lines.
     * @throws Exception If there is some problem inside
     */
    @Test
    void ignoresBrokenLines() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("bobby:")
            ).authorized("bobby", "some-pwd-9"),
            Matchers.is(false)
        );
    }

    /**
     * Htpasswd can use default host.
     * @throws Exception If there is some problem inside
     */
    @Test
    void worksWithDefaultHost() throws Exception {
        final Htpasswd htpasswd = new Htpasswd(
            new DefaultHost(
                new BucketMocker().init().mock(),
                () -> Mockito.mock(AmazonCloudWatchClient.class)
            )
        );
        MatcherAssert.assertThat(
            htpasswd.authorized("jacky", "pwd"),
            Matchers.is(false)
        );
    }

    /**
     * Create host that fetches the provided htpasswd content.
     * @param htpasswd The content to fetch
     * @return The host
     * @throws Exception If there is some problem inside
     */
    private Host host(final String htpasswd) throws Exception {
        final Host host = Mockito.spy(new HostMocker().init().mock());
        Mockito.doReturn(new ResourceMocker().init().withContent(htpasswd).mock())
            .when(host)
            .fetch(URI.create("/.htpasswd"), Range.ENTIRE, Version.LATEST);
        return host;
    }

    /**
     * Mock Host.Cloudwatch for test.
     * @return Mock CloudWatch
     */
    private CloudWatch cloudWatch() {
        return () -> Mockito.mock(AmazonCloudWatchClient.class);
    }
}
