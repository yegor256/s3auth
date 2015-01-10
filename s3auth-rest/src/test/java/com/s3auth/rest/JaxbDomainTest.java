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
package com.s3auth.rest;

import com.jcabi.matchers.JaxbConverter;
import com.jcabi.matchers.XhtmlMatchers;
import com.rexsl.mock.UriInfoMocker;
import com.s3auth.hosts.Domain;
import com.s3auth.hosts.Stats;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

/**
 * Test case for {@link JaxbDomain}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class JaxbDomainTest {

    /**
     * JaxbDomain can be converted to XML.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToXml() throws Exception {
        final JaxbDomain obj = new JaxbDomain(
            // @checkstyle AnonInnerLength (50 lines)
            new Domain() {
                @Override
                public String name() {
                    return "localhost";
                }
                @Override
                public String key() {
                    return "ABC";
                }
                @Override
                public String secret() {
                    return "foo";
                }
                @Override
                public String bucket() {
                    return "def";
                }
                @Override
                public String region() {
                    return "s3-sa-east-1";
                }
                @Override
                public String syslog() {
                    return "localhost:12345";
                }
            },
            new UriInfoMocker().mock(),
            new Stats() {
                @Override
                public long bytesTransferred() {
                    //@checkstyle MagicNumber (1 line)
                    return 42L;
                }
            }
        );
        MatcherAssert.assertThat(
            JaxbConverter.the(obj),
            XhtmlMatchers.hasXPaths(
                "/domain[name='localhost']",
                "/domain[key='ABC']",
                "/domain[secret='foo']",
                "/domain[bucket='def']",
                "/domain[region='s3-sa-east-1']",
                "/domain[syslog='localhost:12345']",
                "/domain/links/link[@rel='remove']",
                "/domain/stats[bytesTransferred='42']"
            )
        );
    }

}
