/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.Md5Crypt;

/**
 * MD5 hash builder.
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class Md5 implements Htpasswd.Algorithm {

    /**
     * MD5 pattern.
     */
    private static final Pattern PATTERN =
        Pattern.compile("\\$apr1\\$([^\\$]+)\\$([a-zA-Z0-9/\\.]+=*)");

    @Override
    public boolean matches(final String hash, final String password) {
        final Matcher matcher = Md5.PATTERN.matcher(hash);
        final boolean matches;
        if (matcher.matches()) {
            matches = hash.equals(
                Md5Crypt.apr1Crypt(password, matcher.group(1))
            );
        } else {
            matches = false;
        }
        return matches;
    }
}
