/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.Crypt;

/**
 * UNIX crypt.
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class UnixCrypt implements Htpasswd.Algorithm {

    /**
     * Unix Crypt pattern.
     */
    private static final Pattern PATTERN =
        Pattern.compile("(\\$[156]\\$)?[a-zA-Z0-9./]+(\\$.*)*");

    @Override
    public boolean matches(final String hash, final String password) {
        return UnixCrypt.PATTERN.matcher(hash).matches()
            && hash.equals(Crypt.crypt(password, hash));
    }
}
