/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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

import com.s3auth.hosts.Domain;
import com.s3auth.hosts.Hosts;
import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.misc.Href;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDirectives;
import org.takes.rs.xe.XeLink;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeTransform;
import org.xembly.Directives;

/**
 * Index page of a logged in user.
 *
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
final class TkIndex implements Take {

    /**
     * Hosts.
     */
    private final transient Hosts hosts;

    /**
     * Ctor.
     * @param hsts Hosts
     */
    TkIndex(final Hosts hsts) {
        this.hosts = hsts;
    }

    @Override
    public Response act(final Request request) throws IOException {
        return new RsPage(
            "/xsl/index.xsl",
            request,
            new XeLink("add", "/add"),
            new XeAppend(
                "domains",
                new XeTransform<Domain>(
                    this.hosts.domains(new RqUser(request).user()),
                    new XeTransform.Func<Domain>() {
                        @Override
                        public XeSource transform(final Domain domain) {
                            return TkIndex.source(domain);
                        }
                    }
                )
            )
        );
    }

    /**
     * Convert domain to Xembly source.
     * @param domain The domain
     * @return Source
     */
    private static XeSource source(final Domain domain) {
        return new XeAppend(
            "domain",
            new XeChain(
                new XeDirectives(
                    new Directives()
                        .add("name").set(domain.name()).up()
                        .add("key").set(domain.key()).up()
                        .add("secret").set(domain.secret()).up()
                        .add("bucket").set(domain.bucket()).up()
                        .add("region").set(domain.region()).up()
                        .add("syslog").set(domain.syslog())
                ),
                new XeLink(
                    // @checkstyle MultipleStringLiteralsCheck (1 line)
                    "remove",
                    new Href().path("remove").with("host", domain.name())
                )
            )
        );
    }

}
