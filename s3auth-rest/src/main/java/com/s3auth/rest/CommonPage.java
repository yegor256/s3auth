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
import com.jcabi.manifests.Manifests;
import com.rexsl.page.BasePage;
import com.rexsl.page.JaxbBundle;
import com.rexsl.page.Link;
import com.s3auth.hosts.User;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base RESTful page.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@XmlRootElement(name = "page")
@XmlAccessorType(XmlAccessType.NONE)
public class CommonPage extends BasePage<CommonPage, BaseRs> {

    /**
     * Set authenticated user.
     * @param user The user
     * @return Itself
     */
    @Loggable(Loggable.DEBUG)
    public final CommonPage authenticated(final User user) {
        this.link(new Link("logout", "/a/out"));
        this.append(new JaxbUser(user));
        return this;
    }

    /**
     * Render it.
     * @return JAX-RS response
     */
    @Loggable(Loggable.DEBUG)
    public final Response.ResponseBuilder render() {
        BaseRs.class.cast(this.home()).render(this);
        final Response.ResponseBuilder builder = Response.ok();
        this.append(
            new JaxbBundle("version")
                .add("name", Manifests.read("S3Auth-Version"))
                .up()
                .add("revision", Manifests.read("S3Auth-Revision"))
                .up()
                .add("date", Manifests.read("S3Auth-Date"))
                .up()
        );
        builder.entity(this);
        builder.type(MediaType.TEXT_XML);
        builder.header(HttpHeaders.VARY, "Cookie");
        BaseRs.class.cast(this.home()).render(builder);
        return builder;
    }

}
