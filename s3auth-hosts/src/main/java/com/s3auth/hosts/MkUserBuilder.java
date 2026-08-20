/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.urn.URN;
import java.net.URI;

/**
 * Builder for MkUser.
 * @since 0.0.1
 */
final class MkUserBuilder {

    /**
     * The User identity.
     */
    private URN identity;

    /**
     * The user name.
     */
    private String name;

    /**
     * The User photo.
     */
    private URI photo;

    /**
     * Set identity.
     * @param value The identity
     * @return This builder
     */
    MkUserBuilder identity(final URN value) {
        this.identity = value;
        return this;
    }

    /**
     * Set name.
     * @param value The name
     * @return This builder
     */
    MkUserBuilder name(final String value) {
        this.name = value;
        return this;
    }

    /**
     * Set photo.
     * @param value The photo
     * @return This builder
     */
    MkUserBuilder photo(final URI value) {
        this.photo = value;
        return this;
    }

    /**
     * Build the user.
     * @return The user
     */
    MkUser build() {
        return new MkUser(this.identity, this.name, this.photo);
    }
}
