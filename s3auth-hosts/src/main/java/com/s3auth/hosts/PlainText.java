/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;

/**
 * Plain Text.
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class PlainText implements Htpasswd.Algorithm {

    @Override
    public boolean matches(final String hash, final String password) {
        return password.equals(hash);
    }
}
