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
package com.s3auth.hosts;

import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.s3auth.hosts.Host.CloudWatch;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Integration test case for {@link DefaultHost}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class DefaultHostITCase {

    /**
     * DefaultHost can fetch a real object from S3 bucket.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fetchesRealObjectFromAmazonBucket() throws Exception {
        final String key = System.getProperty("failsafe.aws.key");
        final String secret = System.getProperty("failsafe.aws.secret");
        Assume.assumeThat(key, Matchers.notNullValue());
        final Host host = new DefaultHost(
            new DefaultBucket(
                new DomainMocker()
                    // @checkstyle MultipleStringLiterals (1 line)
                    .withName("maven.s3auth.com")
                    .withKey(key)
                    .withSecret(secret)
                    .withBucket("maven.s3auth.com")
                    .withRegion("s3")
                    .mock()
            ),
            this.cloudWatch()
        );
        MatcherAssert.assertThat(
            ResourceMocker.toString(
                // @checkstyle MagicNumber (3 lines)
                host.fetch(
                    URI.create("/index.html"),
                    new Range.Simple(3, 500),
                    Version.LATEST
                )
            ),
            Matchers.startsWith("OCTYPE html>\n")
        );
    }

    /**
     * DefaultHost can throw IOException for absent object.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = java.io.IOException.class)
    public void throwsWhenAbsentResource() throws Exception {
        final Host host = new DefaultHost(
            new DefaultBucket(
                new DomainMocker()
                    .withName("invalid-bucket.s3auth.com")
                    .withKey("foo")
                    .withSecret("invalid-data")
                    .mock()
            ),
            this.cloudWatch()
        );
        host.fetch(URI.create("foo.html"), Range.ENTIRE, Version.LATEST);
    }

    /**
     * Mock CloudWatch for test.
     *
     * @return Mock cloudwatch
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
