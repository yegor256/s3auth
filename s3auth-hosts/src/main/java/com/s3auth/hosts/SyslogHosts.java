/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.productivity.java.syslog4j.SyslogIF;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslog;
import org.productivity.java.syslog4j.impl.net.udp.UDPNetSyslogConfig;

/**
 * Decorator of {@link Hosts}, adds syslog capabilities for each domain.
 *
 * <p>The class is immutable and thread-safe.</p>
 * @since 0.0.1
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "hosts")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class SyslogHosts implements Hosts {

    /**
     * The underlying Hosts instance.
     */
    private final transient Hosts hosts;

    /**
     * Public ctor.
     * @param hsts The hosts to add syslog capability to
     */
    public SyslogHosts(final Hosts hsts) {
        this.hosts = hsts;
    }

    @Override
    public void close() throws IOException {
        this.hosts.close();
    }

    @Override
    public Host find(final String domain) throws IOException {
        return new SyslogHost(this.hosts.find(domain));
    }

    @Override
    public Set<Domain> domains(final User user) throws IOException {
        return this.hosts.domains(user);
    }

    /**
     * Syslog host wrapper.
     *
     * @since 0.0.1
     */
    @Immutable
    @EqualsAndHashCode(of = "host")
    private static final class SyslogHost implements Host {
        /**
         * Pattern for matching syslog host and port.
         */
        private static final Pattern PATTERN = Pattern.compile(
            "(\\w+)(:(\\d+))?"
        );

        /**
         * The underlying Host instance.
         */
        private final transient Host host;

        /**
         * Constructor.
         * @param hst Host
         */
        SyslogHost(final Host hst) {
            this.host = hst;
        }

        @Override
        public void close() throws IOException {
            this.host.close();
        }

        @Override
        public Resource fetch(final URI uri, final Range range,
            final Version version) throws IOException {
            final Matcher matcher = PATTERN.matcher(this.host.syslog());
            final Resource res;
            if (matcher.find()) {
                final String syslg = matcher.group(1);
                final int port;
                if (matcher.group(3) == null) {
                    port = 514;
                } else {
                    port = Integer.parseInt(matcher.group(3));
                }
                res = new SyslogResource(
                    this.host.fetch(uri, range, version), uri, syslg, port
                );
            } else {
                res = this.host.fetch(uri, range, version);
            }
            return res;
        }

        @Override
        public boolean isHidden(final URI uri) throws IOException {
            return this.host.isHidden(uri);
        }

        @Override
        public boolean authorized(final String user, final String password)
            throws IOException {
            return this.host.authorized(user, password);
        }

        @Override
        public String syslog() {
            return this.host.syslog();
        }

        @Override
        public Stats stats() {
            return this.host.stats();
        }
    }

    /**
     * Syslog Resource wrapper.
     *
     * @since 0.0.1
     */
    private static final class SyslogResource implements Resource {
        /**
         * The underlying resource.
         */
        private final transient Resource resource;

        /**
         * URI of the resource.
         */
        private final transient URI location;

        /**
         * The syslog client.
         */
        private final transient SyslogIF syslog;

        /**
         * Constructor.
         * @param res The underlying resource
         * @param uri The URI to fetch
         * @param syslg The syslog host
         * @param port The syslog port
         * @checkstyle ParameterNumber (4 lines)
         */
        @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
        SyslogResource(final Resource res, final URI uri, final String syslg,
            final int port) {
            this.resource = res;
            this.location = uri;
            this.syslog = new UDPNetSyslog();
            this.syslog.initialize("udp", new UDPNetSyslogConfig(syslg, port));
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
                this.syslog.info(
                    String.format(
                        "Obtained %d bytes from %s", bytes, this.location
                    )
                );
            } catch (final IOException exp) {
                this.syslog.error(
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
    }

}
