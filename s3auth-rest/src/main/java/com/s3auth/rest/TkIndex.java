/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
                new XeTransform<>(
                    this.hosts.domains(new RqUser(request).user()),
                    TkIndex::source
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
                    "remove",
                    new Href().path("remove").with("host", domain.name())
                )
            )
        );
    }

}
