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
package com.s3auth.rest;

import com.rexsl.test.JaxbConverter;
import com.rexsl.test.XhtmlMatchers;
import com.s3auth.hosts.User;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link CryptedUser}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class CryptedUserTest {

    /**
     * CryptedUser can be converted to text and back.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsToTextAndBack() throws Exception {
        final String name = "John Doe, \u0433";
        final String identity = "somebody";
        final CryptedUser user = new CryptedUser(
            new User() {
                @Override
                public String identity() {
                    return identity;
                }
                @Override
                public String name() {
                    return name;
                }
                @Override
                public URI photo() {
                    return URI.create("#");
                }
            }
        );
        final User reverted = CryptedUser.valueOf(user.toString());
        MatcherAssert.assertThat(
            reverted.name(),
            Matchers.equalTo(name)
        );
        MatcherAssert.assertThat(
            reverted.identity(),
            Matchers.equalTo(identity)
        );
    }

    /**
     * CryptedUser can throw on NULL.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = CryptedUser.DecryptionException.class)
    public void throwsOnNullInput() throws Exception {
        CryptedUser.valueOf(null);
    }

    /**
     * CryptedUser can throw on invalid input.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = CryptedUser.DecryptionException.class)
    public void throwsOnBrokenInput() throws Exception {
        CryptedUser.valueOf("invalid-data");
    }

    /**
     * CryptedUser can throw on empty input.
     * @throws Exception If there is some problem inside
     */
    @Test(expected = CryptedUser.DecryptionException.class)
    public void throwsOnEmptyInput() throws Exception {
        CryptedUser.valueOf("");
    }

}
