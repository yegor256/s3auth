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
import org.takes.rq.RqHref;

/**
 * Remove a domain.
 *
 * @since 0.1
 */
final class TkRemove implements Take {

    /**
     * Hosts.
     */
    private final transient Hosts hosts;

    /**
     * Ctor.
     * @param hsts Hosts
     */
    TkRemove(final Hosts hsts) {
        this.hosts = hsts;
    }

    @Override
    public Response act(final Request request) throws IOException {
        final User user = new RqUser(request).user();
        final String host = new RqHref.Base(request).href()
            .param("host").iterator().next();
        final boolean removed = this.hosts.domains(user).remove(
            new SimpleDomain(host)
        );
        if (!removed) {
            throw new RsForward(
                new RsFlash(
                    String.format(
                        "failed to remove '%s' host", host
                    ),
                    Level.WARNING
                )
            );
        }
        return new RsForward(
            new RsFlash(
                String.format("removed '%s' host from collection", host)
            )
        );
    }

}
