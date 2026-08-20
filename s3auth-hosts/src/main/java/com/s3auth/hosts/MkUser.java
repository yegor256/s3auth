/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.urn.URN;
import java.net.URI;

/**
 * Mock.
 * @since 0.0.1
 */
final class MkUser implements User {

    /**
     * The User identity.
     */
    private final transient URN identity;

    /**
     * The user name.
     */
    private final transient String name;

    /**
     * The User photo.
     */
    private final transient URI photo;

    /**
     * Constructor.
     * @param ident The identity
     * @param usr The name
     * @param pht The photo
     */
    MkUser(final URN ident, final String usr, final URI pht) {
        this.identity = ident;
        this.name = usr;
        this.photo = pht;
    }

    @Override
    public URN identity() {
        return this.identity;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public URI photo() {
        return this.photo;
    }
}
