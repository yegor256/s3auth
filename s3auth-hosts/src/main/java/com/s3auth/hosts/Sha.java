/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Loggable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * SHA1 hash builder.
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class Sha implements Htpasswd.Algorithm {

    /**
     * SHA1 pattern.
     */
    private static final Pattern PATTERN =
        Pattern.compile("\\{SHA\\}([a-zA-Z0-9/\\+]+=*)");

    @Override
    public boolean matches(final String hash, final String password) {
        final Matcher matcher = Sha.PATTERN.matcher(hash);
        final boolean matches;
        if (matcher.matches()) {
            matches = matcher.group(1).equals(
                Base64.encodeBase64String(DigestUtils.sha1(password))
            );
        } else {
            matches = false;
        }
        return matches;
    }
}
