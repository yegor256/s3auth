/*
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
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * A {@link Host} that adds extra information on top of bucket's
 * original content.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.0.1
 */
@Immutable
@EqualsAndHashCode(of = "host")
@Loggable(Loggable.DEBUG)
final class SmartHost implements Host {

    /**
     * Pattern for htpasswd matching.
     */
    private static final Pattern HTPASSWD = Pattern.compile("^/?\\.htpasswd$");

    /**
     * The original host.
     */
    private final transient Host host;

    /**
     * Public ctor.
     * @param hst Original host
     */
    SmartHost(@NotNull final Host hst) {
        this.host = hst;
    }

    @Override
    public String toString() {
        return this.host.toString();
    }

    @Override
    public void close() throws IOException {
        this.host.close();
    }

    @Override
    public String syslog() {
        return this.host.syslog();
    }

    @Override
    public Stats stats() {
        return this.host.stats();
    }

    @Override
    @Loggable(value = Loggable.DEBUG, ignore = IOException.class)
    public Resource fetch(@NotNull final URI uri, @NotNull final Range range,
        @NotNull final Version version) throws IOException {
        final Resource resource;
        if (SmartHost.HTPASSWD.matcher(uri.toString()).matches()) {
            String text;
            try {
                final Resource htpasswd = this.host.fetch(uri, range, version);
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                htpasswd.writeTo(baos);
                text = String.format(
                    "%d byte(s)",
                    baos.toByteArray().length
                );
            } catch (final IOException ex) {
                text = Logger.format("%[exception]s", ex);
            }
            resource = new Resource.PlainText(text);
        } else {
            resource = this.host.fetch(uri, range, version);
        }
        return resource;
    }

    @Override
    public boolean isHidden(@NotNull final URI uri) {
        return !SmartHost.HTPASSWD.matcher(uri.toString()).matches();
    }

    @Override
    public boolean authorized(@NotNull final String user,
        @NotNull final String password) throws IOException {
        return this.host.authorized(user, password);
    }

}
