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

import org.mockito.Mockito;

/**
 * Mocker of {@link Domain}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class DomainMocker {

    /**
     * The mock.
     */
    private final transient Domain domain = Mockito.mock(Domain.class);

    /**
     * Public ctor.
     */
    public DomainMocker() {
        this.withName("localhost");
        this.withKey("AKIAJFWVOY5KEEZNZXAQ");
        this.withSecret("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");
    }

    /**
     * With this name.
     * @param name The name
     * @return This object
     */
    public DomainMocker withName(final String name) {
        Mockito.doReturn(name).when(this.domain).name();
        return this;
    }

    /**
     * With this key.
     * @param key The key
     * @return This object
     */
    public DomainMocker withKey(final String key) {
        Mockito.doReturn(key).when(this.domain).key();
        return this;
    }

    /**
     * With this secret.
     * @param secret The secret
     * @return This object
     */
    public DomainMocker withSecret(final String secret) {
        Mockito.doReturn(secret).when(this.domain).secret();
        return this;
    }

    /**
     * Mock it.
     * @return The domain
     */
    public Domain mock() {
        return this.domain;
    }

}
