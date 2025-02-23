/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
