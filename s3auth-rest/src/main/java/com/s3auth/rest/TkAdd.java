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

/**
 * Add a domain.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
        final RqForm form = new RqForm(request);
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
