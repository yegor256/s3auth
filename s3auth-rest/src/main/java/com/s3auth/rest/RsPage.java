/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import org.takes.rs.RsXslt;
import org.takes.rs.xe.RsXembly;
import org.takes.rs.xe.XeAppend;
import org.takes.rs.xe.XeChain;
import org.takes.rs.xe.XeDate;
import org.takes.rs.xe.XeLinkHome;
import org.takes.rs.xe.XeLinkSelf;
import org.takes.rs.xe.XeLocalhost;
import org.takes.rs.xe.XeMillis;
import org.takes.rs.xe.XeSla;
import org.takes.rs.xe.XeSource;
import org.takes.rs.xe.XeStylesheet;

/**
 * Index resource, front page of the website.
 *
 * @since 0.1
 */
@EqualsAndHashCode(callSuper = true)
final class RsPage extends RsWrap {

    /**
     * Version of the system, to show in header.
     */
    private static final String VERSION_LABEL = String.format(
        "%s/%s built on %s",
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
                new XeLinkHome(req),
                new XeLinkSelf(req),
                new XeDate(),
                new XeSla(),
                new XeLocalhost(),
                new XeIdentity(req),
                new XeFlash(req),
                new XeGithubLink(req, Manifests.read("S3Auth-GithubId")),
                new XeFacebookLink(req, Manifests.read("S3Auth-FbId")),
                new XeGoogleLink(
                    req, Manifests.read("S3Auth-GoogleId"),
                    "https://www.s3auth.com/?PsByFlag=PsGoogle"
                ),
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
                    new RsXslt(new RsWithType(raw, "text/html"))
                )
            ),
            "X-S3Auth-Version",
            RsPage.VERSION_LABEL
        );
    }

}
