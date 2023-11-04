/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Timeable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * A {@link Host} that does everything fast.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.2
 */
@Immutable
@EqualsAndHashCode(of = "origin")
final class FastHost implements Host {

    /**
     * The original host.
     */
    private final transient Host origin;

    /**
     * Public ctor.
     * @param hst Original host
     */
    FastHost(final Host hst) {
        this.origin = hst;
    }

    @Override
    public String toString() {
        return this.origin.toString();
    }

    @Override
    public void close() throws IOException {
        this.origin.close();
    }

    @Override
    public String syslog() {
        return this.origin.syslog();
    }

    @Override
    public Stats stats() {
        return this.origin.stats();
    }

    @Override
    @Timeable(limit = 2, unit = TimeUnit.MINUTES)
    public Resource fetch(final URI uri, final Range range,
        final Version version) throws IOException {
        return this.origin.fetch(uri, range, version);
    }

    @Override
    public boolean isHidden(final URI uri) throws IOException {
        return this.origin.isHidden(uri);
    }

    @Override
    public boolean authorized(final String user,
        final String password) throws IOException {
        return this.origin.authorized(user, password);
    }

}
