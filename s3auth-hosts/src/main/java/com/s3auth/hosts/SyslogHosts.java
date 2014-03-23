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
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @todo #34 SyslogHosts should add syslog capabilities to the operations of its
 *  underlying Hosts instance. The way that I imagine that this will be done is
 *  that {@link SyslogHosts#find(String)}) should create instances of Host,
 *  in turn {@link Host#fetch(java.net.URI, Range)} will create instances of
 *  {@link Resource} that sends messages to the syslog host when
 *  {@link Resource#writeTo(java.io.OutputStream)} is invoked. I'm making a few
 *  assumptions about what remains to be done here, namely: 1) The Host and
 *  Resource decorator objects will probably be inner classes within Hosts or
 *  find, or even anonymous inner classes - whatever seems best, and 2) the
 *  underlying Hosts' close() and domains() operations will be unchanged, and
 *  thus I made simple implementations that directly delegate to the underlying
 *  instance. Go ahead and change any of the above if the initial design
 *  considerations are incorrect.
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
                // @checkstyle MagicNumber (4 lines)
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
    }

    /**
     * Syslog Resource wrapper.
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
            long bytes = 0L;
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
    }

}
