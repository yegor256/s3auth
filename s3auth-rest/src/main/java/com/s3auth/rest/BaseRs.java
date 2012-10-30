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
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Resource;
import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.User;
import javax.servlet.ServletContext;
import javax.ws.rs.CookieParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
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
public class BaseRs extends BaseResource {

    /**
     * Name of auth cookie.
     */
    public static final String COOKIE = "s3auth.com";

    /**
     * Hosts.
     */
    private transient Hosts ihosts;

    /**
     * Cookie.
     */
    private transient String icookie;

    /**
     * Flash message from previous page.
     */
    private transient FlashCookie flash;

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
     * Set flash cookie. Should be called by JAX-RS implemenation
     * because of {@code &#64;CookieParam} annotation.
     * @param cookie The cookie to set
     */
    @CookieParam(FlashCookie.NAME)
    public final void setFlash(final String cookie) {
        if (cookie != null && !cookie.isEmpty()) {
            this.flash = new FlashCookie(cookie);
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
     * Render something extra to the page.
     * @param page The page to render into
     */
    public final void render(final CommonPage page) {
        if (this.flash != null) {
            page.append(
                new JaxbBundle("flash")
                    .add("message", this.flash.message())
                    .up()
                    .add("color", this.flash.color())
                    .up()
            );
        }
    }

    /**
     * Render something extra to the builder.
     * @param builder Response builder
     */
    public final void render(final Response.ResponseBuilder builder) {
        if (this.flash != null) {
            this.flash.clean(builder, this.uriInfo().getBaseUri());
        }
    }

    /**
     * Get current user.
     * @return Name of the user
     */
    protected final User user() {
        try {
            return CryptedUser.valueOf(this.icookie);
        } catch (CryptedUser.DecryptionException ex) {
            Logger.debug(
                this,
                "Decryption failure from %s calling '%s': %[exception]s",
                this.httpServletRequest().getRemoteAddr(),
                this.httpServletRequest().getRequestURI(),
                ex
            );
            throw new WebApplicationException(
                ex,
                Response.status(Response.Status.TEMPORARY_REDIRECT)
                    .header("s3auth-error", ex.getMessage())
                    .location(
                        this.uriInfo().getBaseUriBuilder()
                            .clone()
                            .path("/a")
                            .build()
                    )
                    .build()
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
