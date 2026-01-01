/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import com.s3auth.hosts.Hosts;
import com.s3auth.hosts.User;
import java.io.IOException;
import java.util.logging.Level;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.flash.RsFlash;
import org.takes.facets.forward.RsForward;
import org.takes.rq.RqForm;
import org.takes.rq.form.RqFormBase;

/**
 * Add a domain.
 *
 * @since 0.1
 */
final class TkAdd implements Take {

    /**
     * Hosts.
     */
    private final transient Hosts hosts;

    /**
     * Ctor.
     * @param hsts Hosts
     */
    TkAdd(final Hosts hsts) {
        this.hosts = hsts;
    }

    @Override
    public Response act(final Request request) throws IOException {
        final User user = new RqUser(request).user();
        final RqForm form = new RqFormBase(request);
        final String host = form.param("host").iterator().next();
        final boolean added = this.hosts.domains(user).add(
            new SimpleDomain(
                host,
                form.param("key").iterator().next(),
                form.param("secret").iterator().next(),
                form.param("bucket").iterator().next(),
                form.param("region").iterator().next(),
                form.param("syslog").iterator().next()
            )
        );
        if (!added) {
            throw new RsForward(
                new RsFlash(
                    String.format(
                        "host '%s' is already registered in the system",
                        host
                    ),
                    Level.WARNING
                )
            );
        }
        return new RsForward(
            new RsFlash(
                String.format("added '%s' host to collection", host)
            )
        );
    }

}
