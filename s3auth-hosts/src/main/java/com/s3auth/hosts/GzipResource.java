/*
 * Copyright (c) 2012-2023, Yegor Bugayenko
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

import com.google.common.collect.ImmutableList;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.zip.GZIPOutputStream;
import lombok.EqualsAndHashCode;
import org.apache.http.HttpHeaders;

/**
 * Wrapper for {@link Resource} that writes GZIP compressed output.
 * @since 0.0.1
 */
@Immutable
@EqualsAndHashCode(of = "resource")
@Loggable(Loggable.DEBUG)
public final class GzipResource implements Resource {

    /**
     * The underlying resource.
     */
    private final transient Resource resource;

    /**
     * Public ctor.
     * @param res The underlying resource to compress
     */
    public GzipResource(final Resource res) {
        this.resource = res;
    }

    @Override
    public int status() {
        return this.resource.status();
    }

    @Override
    public long writeTo(final OutputStream stream) throws IOException {
        final GZIPOutputStream gzip = new GZIPOutputStream(stream);
        final long bytes;
        try {
            bytes = this.resource.writeTo(gzip);
        } finally {
            gzip.finish();
        }
        return bytes;
    }

    @Override
    public Collection<String> headers() throws IOException {
        return ImmutableList.<String>builder()
            .addAll(this.resource.headers())
            .add(String.format("%s: %s", HttpHeaders.CONTENT_ENCODING, "gzip"))
            .build();
    }

    @Override
    public String etag() {
        return this.resource.etag();
    }

    @Override
    public Date lastModified() {
        return this.resource.lastModified();
    }

    @Override
    public String contentType() {
        return this.resource.contentType();
    }

    @Override
    public void close() throws IOException {
        this.resource.close();
    }

}
