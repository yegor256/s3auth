/**
 * Copyright (c) 2012-2014, s3auth.com
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

import com.jcabi.aspects.Loggable;
import com.jcabi.manifests.Manifests;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import com.s3auth.hosts.Domain;
import com.s3auth.hosts.Stats;
import com.s3auth.hosts.User;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Friends finding service (used by RESTful client or AJAX).
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@Path("/")
@SuppressWarnings("PMD.TooManyMethods")
@Loggable(Loggable.DEBUG)
public final class IndexRs extends BaseRs {

    /**
     * Get list of all my domains.
     * @return The JAX-RS response
     * @throws IOException If some IO problem inside
     */
    @GET
    @Path("/")
    public Response index() throws IOException {
        return new PageBuilder()
            .stylesheet("/xsl/index.xsl")
            .build(CommonPage.class)
            .init(this)
            .append(JaxbGroup.build(this.domains(), "domains"))
            .link(new Link("add", "/add"))
            .render()
            .build();
    }

    /**
     * Add new domain.
     * @param host The host name
     * @param key AWS key
     * @param secret AWS secret
     * @param bucket Bucket name
     * @param region S3 region
     * @param syslog The syslog host and port
     * @return The JAX-RS response
     * @throws IOException If some IO problem inside
     * @checkstyle ParameterNumber (9 lines)
     */
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public Response add(
        @FormParam("host") final String host,
        @FormParam("key") final String key,
        @FormParam("secret") final String secret,
        @FormParam("bucket") final String bucket,
        @DefaultValue("s3") @FormParam("region") final String region,
        @DefaultValue("syslog.s3auth.com:514") @FormParam("syslog")
        final String syslog)
        throws IOException {
        final User user = this.user();
        if (user.equals(User.ANONYMOUS)) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                "please login first, in order to add new domains",
                Level.SEVERE
            );
        }
        final boolean added = this.hosts().domains(user).add(
            new SimpleDomain(host, key, secret, bucket, region, syslog)
        );
        if (!added) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                String.format(
                    "host '%s' is already registered in the system",
                    host
                ),
                Level.WARNING
            );
        }
        return FlashInset.forward(
            this.uriInfo().getBaseUri(),
            String.format("added '%s' host to collection", host),
            Level.INFO
        ).getResponse();
    }

    /**
     * Delete existing domain.
     * @param host The host name
     * @return The JAX-RS response
     * @throws IOException If some IO problem inside
     */
    @GET
    @Path("/remove")
    public Response remove(@QueryParam("host") final String host)
        throws IOException {
        final User user = this.user();
        if (user.equals(User.ANONYMOUS)) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                "please login first, to be able to delete domains",
                Level.SEVERE
            );
        }
        final boolean removed = this.hosts().domains(user).remove(
            new SimpleDomain(host, "", "", "", "", "")
        );
        if (!removed) {
            throw FlashInset.forward(
                this.uriInfo().getBaseUri(),
                String.format(
                    "failed to remove '%s' host",
                    host
                ),
                Level.WARNING
            );
        }
        return FlashInset.forward(
            this.uriInfo().getBaseUri(),
            String.format("removed '%s' host from collection", host),
            Level.INFO
        ).getResponse();
    }

    /**
     * Get list of all my domains.
     * @return List of JAXB domains
     * @throws IOException If some IO problem inside
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Collection<JaxbDomain> domains() throws IOException {
        final Collection<JaxbDomain> domains = new LinkedList<JaxbDomain>();
        for (final Domain domain : this.hosts().domains(this.user())) {
            final Stats stats;
            if (
                "AAAAAAAAAAAAAAAAAAAA"
                    .equals(Manifests.read("S3Auth-AwsCloudWatchKey"))
            ) {
                stats = new DummyStats();
            } else {
                stats = this.hosts().find(domain.name()).stats();
            }
            domains.add(
                new JaxbDomain(
                    domain,
                    this.uriInfo(),
                    stats
                )
            );
        }
        return domains;
    }

    /**
     * Simple domain representation.
     */
    private static final class SimpleDomain implements Domain {
        /**
         * Host.
         */
        private final transient String host;
        /**
         * Key.
         */
        private final transient String acc;
        /**
         * Secret.
         */
        private final transient String sec;
        /**
         * Bucket name.
         */
        private final transient String buckt;
        /**
         * Region.
         */
        private final transient String regn;
        /**
         * Syslog host.
         */
        private final transient String slog;
        /**
         * Constructor.
         * @param hst The host name
         * @param access AWS access key
         * @param scrt AWS secret
         * @param bckt Bucket name
         * @param rgn S3 region
         * @param syslg The syslog host and port
         * @checkstyle ParameterNumber (4 lines)
         */
        SimpleDomain(final String hst, final String access, final String scrt,
            final String bckt, final String rgn, final String syslg) {
            this.host = hst;
            this.acc = access;
            this.sec = scrt;
            if (bckt == null) {
                this.buckt = hst;
            } else {
                this.buckt = bckt;
            }
            this.regn = rgn;
            if (syslg.isEmpty()) {
                this.slog = "syslog.s3auth.com:514";
            } else {
                this.slog = syslg;
            }
        }

        @Override
        public String name() {
            return this.host;
        }
        @Override
        public String key() {
            return this.acc;
        }
        @Override
        public String secret() {
            return this.sec;
        }
        @Override
        public String bucket() {
            return this.buckt;
        }
        @Override
        public String region() {
            return this.regn;
        }
        @Override
        public String syslog() {
            return this.slog;
        }
    }

    /**
     * Dummy stats, if AWS credentials are invalid.
     */
    private static final class DummyStats implements Stats {
        @Override
        public long bytesTransferred() {
            return 0L;
        }
    }

}
