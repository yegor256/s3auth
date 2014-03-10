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
package com.s3auth.hosts;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.s3auth.hosts.Host.CloudWatch;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link Htpasswd}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class HtpasswdTest {

    /**
     * Htpasswd can show some stats in {@code #toString()}.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void showsStatsInToString() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(this.host("peter-pen:hello")),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * Htpasswd can show some stats in {@code #toString()} with IO exception.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void showsStatsInToStringWithIOException() throws Exception {
        final AmazonS3 aws = Mockito.mock(AmazonS3.class);
        Mockito.doThrow(new AmazonClientException("")).when(aws)
            .getObject(Mockito.any(GetObjectRequest.class));
        MatcherAssert.assertThat(
            new Htpasswd(
                new DefaultHost(
                    new BucketMocker().withClient(aws).mock(),
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
    public void understandsApacheNativeHashValues() throws Exception {
        MatcherAssert.assertThat(
            new Htpasswd(
                this.host("foo:$apr1$1/yqU0TM$fx36ZuZIapXW39ivIA5AR.")
            ).authorized("foo", "test"),
            Matchers.is(true)
        );
    }

    /**
     * Htpasswd can manage apache hashes, with SHA1 algorithm.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void understandsShaHashValues() throws Exception {
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
    public void understandsPlainTextHashValues() throws Exception {
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
     * @link http://jxutil.sourceforge.net/API/org/sourceforge/jxutil/JCrypt.html
     * @link http://www.dynamic.net.au/christos/crypt/
     */
    @Test
    public void understandsCryptHashValues() throws Exception {
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
    public void ignoresBrokenLines() throws Exception {
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
    public void worksWithDefaultHost() throws Exception {
        final Htpasswd htpasswd = new Htpasswd(
            new DefaultHost(
                new BucketMocker().mock(),
                new Host.CloudWatch() {
                    @Override
                    public AmazonCloudWatchClient get() {
                        return Mockito.mock(AmazonCloudWatchClient.class);
                    }
                }
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
        final Host host = new HostMocker().mock();
        Mockito.doReturn(new ResourceMocker().withContent(htpasswd).mock())
            .when(host)
            .fetch(URI.create("/.htpasswd"), Range.ENTIRE, Version.LATEST);
        return host;
    }

    /**
     * Mock Host.Cloudwatch for test.
     * @return Mock CloudWatch
     */
    private CloudWatch cloudWatch() {
        return new Host.CloudWatch() {
            @Override
            public AmazonCloudWatchClient get() {
                return Mockito.mock(AmazonCloudWatchClient.class);
            }
        };
    }
}
