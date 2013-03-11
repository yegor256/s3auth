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

import com.jcabi.manifests.Manifests;
import com.jcabi.urn.URN;
import com.rexsl.page.BasePage;
import com.rexsl.page.BaseResource;
import com.rexsl.page.Inset;
import com.rexsl.page.Resource;
import com.rexsl.page.auth.AuthInset;
import com.rexsl.page.auth.Facebook;
import com.rexsl.page.auth.Google;
import com.rexsl.page.auth.Identity;
import com.rexsl.page.inset.FlashInset;
import com.rexsl.page.inset.LinksInset;
import com.rexsl.page.inset.VersionInset;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.User;
import java.net.URI;
import javax.servlet.ServletContext;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Abstract RESTful resource.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@Resource.Forwarded
@Inset.Default({ LinksInset.class, FlashInset.class })
public class BaseRs extends BaseResource {

    /**
     * Hosts.
     */
    private transient Hosts ihosts;

    /**
     * Inset with a version of the product.
     * @return The inset
     */
    @Inset.Runtime
    public Inset ver() {
        return new VersionInset(
            Manifests.read("S3Auth-Version"),
            Manifests.read("S3Auth-Revision"),
            Manifests.read("S3Auth-Date")
        );
    }

    /**
     * Supplementary inset.
     * @return The inset
     */
    @Inset.Runtime
    public Inset supplementary() {
        return new Inset() {
            @Override
            public void render(final BasePage<?, ?> page,
                final Response.ResponseBuilder builder) {
                builder.type(MediaType.TEXT_XML);
                builder.header(HttpHeaders.VARY, "Cookie");
            }
        };
    }

    /**
     * Authentication inset.
     * @return The inset
     */
    @Inset.Runtime
    public AuthInset auth() {
        return new AuthInset(
            this,
            Manifests.read("S3Auth-SecurityKey"),
            Manifests.read("S3Auth-SecuritySalt")
        )
            .with(
                new Facebook(
                    this,
                    Manifests.read("S3Auth-FbId"),
                    Manifests.read("S3Auth-FbSecret")
                )
            )
            .with(
                new Google(
                    this,
                    Manifests.read("S3Auth-GoogleId"),
                    Manifests.read("S3Auth-GoogleSecret")
                )
            );
    }

    /**
     * Inject servlet context. Should be called by JAX-RS implementation
     * because of {@code &#64;Context} annotation. Servlet attributes are
     * injected into context by {@link com.netbout.servlets.HostsListener}
     * servlet listener.
     * @param context The context
     */
    @Context
    public final void setServletContext(final ServletContext context) {
        this.ihosts = (Hosts) context.getAttribute(Hosts.class.getName());
        if (this.ihosts == null) {
            throw new IllegalStateException("HOSTS is not initialized");
        }
    }

    /**
     * Get current user.
     * @return Name of the user
     */
    protected final User user() {
        final Identity identity = this.auth().identity();
        User user;
        if (identity.equals(Identity.ANONYMOUS)) {
            user = User.ANONYMOUS;
        } else {
            user = new User() {
                @Override
                public URN identity() {
                    return identity.urn();
                }
                @Override
                public String name() {
                    return identity.name();
                }
                @Override
                public URI photo() {
                    return identity.photo();
                }
            };
        }
        return user;
    }

    /**
     * Get hosts.
     * @return The hosts
     */
    protected final Hosts hosts() {
        return this.ihosts;
    }

}
