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

import com.jcabi.log.Logger;
import com.rexsl.page.BaseResource;
import javax.servlet.ServletContext;
import javax.ws.rs.CookieParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 * Abstract RESTful resource.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public class BaseRs extends BaseResource {

    /**
     * Name of auth cookie.
     */
    private static final String COOKIE = "s3auth.com";

    /**
     * Hosts.
     */
    private transient Hosts ihosts;

    /**
     * Cookie.
     */
    private transient String icookie;

    /**
     * Set cookie. Should be called by JAX-RS implemenation
     * because of {@code &#64;CookieParam} annotation.
     * @param cookie The cookie to set
     */
    @CookieParam(BaseRs.COOKIE)
    public final void setCookie(final String cookie) {
        if (cookie != null) {
            this.icookie = cookie;
        }
    }


    /**
     * Inject servlet context. Should be called by JAX-RS implemenation
     * because of {@code &#64;Context} annotation. Servlet attributes are
     * injected into context by {@link com.netbout.servlets.HostsListener}
     * servlet listener.
     * @param context The context
     */
    @Context
    public final void setServletContext(final ServletContext context) {
        this.ihosts = (Hosts) context.getAttribute("com.s3auth.HOSTS");
        if (this.ihosts == null) {
            throw new IllegalStateException("HOSTS is not initialized");
        }
    }

    /**
     * Get current user.
     * @return Name of the user
     */
    protected final User user() {
        try {
            return CryptedUser.valueOf(this.icookie);
        } catch (Crypted.DecryptionException ex) {
            Logger.debug(
                this,
                "Decryption failure from %s calling '%s': %[exception]s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex
            );
            throw new WebApplicationException(
                Response.status(Response.Status.TEMPORARY_REDIRECT)
                    .entity(ex.getMessage())
                    .location(
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path("/a/out")
                            .build()
                    )
                    .build(),
                ex
            );
        }
    }

    /**
     * Get hosts.
     * @return The hosts
     */
    protected final Hosts hosts() {
        return this.ihosts;
    }

}
