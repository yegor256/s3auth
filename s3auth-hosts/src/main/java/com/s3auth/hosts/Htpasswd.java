/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;

/**
 * Htpasswd file abstraction.
 *
 * @since 0.0.1
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "host")
@SuppressWarnings("PMD.UnusedPrivateField")
final class Htpasswd {

    /**
     * Lifetime of HTPASSWD in memory, in minutes.
     */
    private static final int LIFETIME = 5;

    /**
     * All known algorithms.
     * @see <a href="http://httpd.apache.org/docs/2.2/misc/password_encryptions.html">Algorithms supported by Apache</a>
     */
    private static final Htpasswd.Algorithm[] ALGORITHMS = {
        new Htpasswd.Md5(),
        new Htpasswd.Sha(),
        new Htpasswd.UnixCrypt(),
        new Htpasswd.PlainText(),
    };

    /**
     * The host we're working with.
     */
    private final transient Host host;

    /**
     * Public ctor.
     * @param hst The host to work with
     */
    Htpasswd(@NotNull final Host hst) {
        this.host = hst;
    }

    @Override
    public String toString() {
        return Logger.format(
            ".htpasswd(%d user(s), reloaded every %d min)",
            this.fetch().size(),
            Htpasswd.LIFETIME
        );
    }

    /**
     * Can this user login in with this credentials?
     * @param user User name
     * @param password Password
     * @return Yes or no
     * @throws IOException If some error inside
     */
    @LogExceptions
    public boolean authorized(@NotNull final String user,
        @NotNull final String password) throws IOException {
        final ConcurrentMap<String, String> users = this.fetch();
        return users.containsKey(user)
            && Htpasswd.matches(users.get(user), password);
    }

    /**
     * Get map of users and passwords from the host.
     * @return Map of users
     */
    @Cacheable(lifetime = Htpasswd.LIFETIME)
    private ConcurrentMap<String, String> fetch() {
        final ConcurrentMap<String, String> map =
            new ConcurrentHashMap<>(0);
        final String[] lines = this.content().split("\n");
        for (final String line : lines) {
            if (line.isEmpty()) {
                continue;
            }
            final String[] parts = line.trim().split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            map.put(parts[0].trim(), parts[1].trim());
        }
        return map;
    }

    /**
     * Fetch the .htpasswd file, or returns empty string if it's absent.
     * @return Content of .htpasswd file, or empty
     */
    private String content() {
        String content;
        try {
            final Resource res = this.host.fetch(
                URI.create("/.htpasswd"),
                Range.ENTIRE,
                Version.LATEST
            );
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            res.writeTo(baos);
            content = baos.toString(StandardCharsets.UTF_8.name()).trim();
        } catch (final IOException ex) {
            Logger.warn(
                this,
                "#content(): failed to fetch .htpasswd from %s: %s",
                this.host, ex.getMessage()
            );
            content = "";
        }
        return content;
    }

    /**
     * Hash matches the password?
     * @param hash The hash to match
     * @param password Password
     * @return TRUE if they match
     */
    private static boolean matches(final String hash, final String password) {
        boolean matches = false;
        for (final Htpasswd.Algorithm algo : Htpasswd.ALGORITHMS) {
            if (algo.matches(hash, password)) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    /**
     * Algorithm.
     *
     * @since 0.0.1
     */
    private interface Algorithm {
        /**
         * Do they match?
         * @param hash The hash
         * @param password The password
         * @return TRUE if they match
         */
        boolean matches(String hash, String password);
    }

    /**
     * MD5 hash builder.
     *
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private static final class Md5 implements Htpasswd.Algorithm {
        /**
         * MD5 pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("\\$apr1\\$([^\\$]+)\\$([a-zA-Z0-9/\\.]+=*)");

        @Override
        public boolean matches(final String hash, final String password) {
            final Matcher matcher = Htpasswd.Md5.PATTERN.matcher(hash);
            final boolean matches;
            if (matcher.matches()) {
                final String result = Md5Crypt.apr1Crypt(
                    password,
                    matcher.group(1)
                );
                matches = hash.equals(result);
            } else {
                matches = false;
            }
            return matches;
        }
    }

    /**
     * SHA1 hash builder.
     *
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private static final class Sha implements Htpasswd.Algorithm {
        /**
         * SHA1 pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("\\{SHA\\}([a-zA-Z0-9/\\+]+=*)");

        @Override
        public boolean matches(final String hash, final String password) {
            final Matcher matcher = Htpasswd.Sha.PATTERN.matcher(hash);
            final boolean matches;
            if (matcher.matches()) {
                final String required = Base64.encodeBase64String(
                    DigestUtils.sha1(password)
                );
                matches = matcher.group(1).equals(required);
            } else {
                matches = false;
            }
            return matches;
        }
    }

    /**
     * UNIX crypt.
     *
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private static final class UnixCrypt implements Htpasswd.Algorithm {
        /**
         * Unix Crypt pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("(\\$[156]\\$)?[a-zA-Z0-9./]+(\\$.*)*");

        @Override
        public boolean matches(final String hash, final String password) {
            return Htpasswd.UnixCrypt.PATTERN.matcher(hash).matches()
                && hash.equals(Crypt.crypt(password, hash));
        }
    }

    /**
     * Plain Text.
     *
     * @since 0.0.1
     */
    @Loggable(Loggable.DEBUG)
    private static final class PlainText implements Htpasswd.Algorithm {
        @Override
        public boolean matches(final String hash, final String password) {
            return password.equals(hash);
        }
    }

}
