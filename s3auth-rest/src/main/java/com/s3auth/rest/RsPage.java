/**
 * Copyright (c) 2012-2015, s3auth.com
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
import java.io.IOException;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.Response;
import org.takes.facets.auth.XeIdentity;
import org.takes.facets.auth.XeLogoutLink;
import org.takes.facets.auth.social.XeFacebookLink;
import org.takes.facets.auth.social.XeGithubLink;
import org.takes.facets.auth.social.XeGoogleLink;
import org.takes.facets.flash.XeFlash;
import org.takes.facets.fork.FkTypes;
import org.takes.facets.fork.RsFork;
import org.takes.rs.RsWithHeader;
import org.takes.rs.RsWithType;
import org.takes.rs.RsWrap;
import org.takes.rs.RsXSLT;
import org.takes.rs.xe.RsXembly;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDate;
import org.takes.rs.xe.XeLocalhost;
import org.takes.rs.xe.XeMillis;
import org.takes.rs.xe.XeSLA;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeStylesheet;

/**
 * Index resource, front page of the website.
 *
 * @author Yegor Bugayenko (yegor@teamed.io)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@EqualsAndHashCode(callSuper = true)
final class RsPage extends RsWrap {

    /**
     * Version of the system, to show in header.
     */
    private static final String VERSION_LABEL = String.format(
        "%s/%s built on %s",
        // @checkstyle MultipleStringLiterals (3 lines)
        Manifests.read("S3Auth-Version"),
        Manifests.read("S3Auth-Revision"),
        Manifests.read("S3Auth-Date")
    );

    /**
     * Ctor.
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @throws IOException If fails
     */
    RsPage(final String xsl, final Request req,
        final XeSource... src) throws IOException {
        super(RsPage.make(xsl, req, src));
    }

    /**
     * Make it.
     * @param xsl XSL
     * @param req Request
     * @param src Source
     * @return Response
     * @throws IOException If fails
     */
    private static Response make(final String xsl,
        final Request req, final XeSource... src) throws IOException {
        final Response raw = new RsXembly(
            new XeStylesheet(xsl),
            new XeAppend(
                "page",
                new XeMillis(false),
                new XeChain(src),
                new XeMillis(true),
                new XeDate(),
                new XeSLA(),
                new XeLocalhost(),
                new XeIdentity(req),
                new XeFlash(req),
                new XeGithubLink(req, Manifests.read("S3Auth-GithubId")),
                new XeFacebookLink(req, Manifests.read("S3Auth-FbId")),
                new XeGoogleLink(req, Manifests.read("S3Auth-GoogleId")),
                new XeLogoutLink(req),
                new XeAppend(
                    "version",
                    new XeAppend("name", Manifests.read("S3Auth-Version")),
                    new XeAppend("rev", Manifests.read("S3Auth-Revision")),
                    new XeAppend("date", Manifests.read("S3Auth-Date"))
                )
            )
        );
        return new RsWithHeader(
            new RsFork(
                req,
                new FkTypes(
                    "application/xml,text/xml",
                    new RsWithType(raw, "text/xml")
                ),
                new FkTypes(
                    "*/*",
                    new RsXSLT(new RsWithType(raw, "text/html"))
                )
            ),
            "X-S3Auth-Version",
            RsPage.VERSION_LABEL
        );
    }

}
