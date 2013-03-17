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

import com.jcabi.aspects.Loggable;
import com.rexsl.page.JaxbGroup;
import com.rexsl.page.Link;
import com.rexsl.page.PageBuilder;
import com.rexsl.page.inset.FlashInset;
import com.s3auth.hosts.Domain;
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
     * @param region S3 region
     * @return The JAX-RS response
     * @throws IOException If some IO problem inside
     * @checkstyle ParameterNumber (9 lines)
     */
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response add(@FormParam("host") final String host,
        @FormParam("key") final String key,
        @FormParam("secret") final String secret,
        @DefaultValue("s3") @FormParam("region") final String region)
        throws IOException {
        final boolean added = this.hosts().domains(this.user()).add(
            new Domain() {
                @Override
                public String name() {
                    return host;
                }
                @Override
                public String key() {
                    return key;
                }
                @Override
                public String secret() {
                    return secret;
                }
                @Override
                public String region() {
                    return region;
                }
            }
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
        final boolean removed = this.hosts().domains(this.user()).remove(
            new Domain() {
                @Override
                public String name() {
                    return host;
                }
                @Override
                public String key() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String secret() {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String region() {
                    throw new UnsupportedOperationException();
                }
            }
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
        for (Domain domain : this.hosts().domains(this.user())) {
            domains.add(new JaxbDomain(domain, this.uriInfo()));
        }
        return domains;
    }

}
