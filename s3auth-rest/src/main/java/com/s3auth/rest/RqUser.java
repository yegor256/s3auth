/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import com.jcabi.urn.URN;
import com.s3auth.hosts.User;
import java.io.IOException;
import java.net.URI;
import lombok.EqualsAndHashCode;
import org.takes.Request;
import org.takes.facets.auth.Identity;
import org.takes.facets.auth.RqAuth;
import org.takes.rq.RqWrap;

/**
 * User retriever from request.
 *
 * @since 0.1
 */
@EqualsAndHashCode(callSuper = true)
final class RqUser extends RqWrap {

    /**
     * Ctor.
     * @param req Request
     */
    RqUser(final Request req) {
        super(req);
    }

    /**
     * Has alias?
     * @return TRUE if alias is there
     * @throws IOException If fails
     */
    public boolean has() throws IOException {
        return !new RqAuth(this).identity().equals(Identity.ANONYMOUS);
    }

    /**
     * Get user.
     * @return User
     * @throws IOException If fails
     */
    public User user() throws IOException {
        final Identity identity = new RqAuth(this).identity();
        final User user;
        if (identity.equals(Identity.ANONYMOUS)) {
            user = User.ANONYMOUS;
        } else {
            user = new User() {
                @Override
                public URN identity() {
                    return URN.create(identity.urn());
                }

                @Override
                public String name() {
                    return identity.properties().get("name");
                }

                @Override
                public URI photo() {
                    return URI.create(
                        identity.properties().get("avatar")
                    );
                }
            };
        }
        return user;
    }

}
