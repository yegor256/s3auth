/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Cacheable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslog;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

/**
 * Syslog Resource wrapper.
 * @since 0.0.1
 */
final class SyslogResource implements Resource {

    /**
     * The underlying resource.
     */
    private final transient Resource resource;

    /**
     * URI of the resource.
     */
    private final transient URI location;

    /**
     * The syslog host.
     */
    private final transient String syslg;

    /**
     * The syslog port.
     */
    private final transient int port;

    /**
     * Constructor.
     * @param res The underlying resource
     * @param uri The URI to fetch
     * @param host The syslog host
     * @param prt The syslog port
     */
    SyslogResource(final Resource res, final URI uri, final String host,
        final int prt) {
        this.resource = res;
        this.location = uri;
        this.syslg = host;
        this.port = prt;
    }

    @Override
    public int status() {
        return this.resource.status();
    }

    @Override
    public long writeTo(final OutputStream stream) throws IOException {
        final long bytes;
        try {
            bytes = this.resource.writeTo(stream);
            this.syslog().info(
                String.format(
                    "Obtained %d bytes from %s", bytes, this.location
                )
            );
        } catch (final IOException exp) {
            this.syslog().error(
                String.format(
                    "Exception thrown when obtaining %s with message %s",
                    this.location,
                    exp.getMessage()
                )
            );
            throw exp;
        }
        return bytes;
    }

    @Override
    public Collection<String> headers() throws IOException {
        return this.resource.headers();
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

    @Cacheable(forever = true)
    private SyslogIF syslog() {
        final SyslogIF sys = new UDPNetSyslog();
        sys.initialize("udp", new UDPNetSyslogConfig(this.syslg, this.port));
        return sys;
    }
}
