/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.google.common.base.Splitter;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;

/**
 * Htpasswd file abstraction.
 * @since 0.0.1
 */
@Immutable
@Loggable(Loggable.DEBUG)
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
        new Md5(),
        new Sha(),
        new UnixCrypt(),
        new PlainText(),
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

    @Override
    public int hashCode() {
        return Objects.hashCode(this.host);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof Htpasswd
            && Objects.equals(this.host, ((Htpasswd) obj).host);
    }

    /**
     * Can this user login in with this credentials?
     * @param user User name
     * @param password Password
     * @return Yes or no
     * @throws IOException If some error inside
     */
    @LogExceptions
    boolean authorized(@NotNull final String user,
        @NotNull final String password) throws IOException {
        final ConcurrentMap<String, String> users = this.fetch();
        return users.containsKey(user)
            && Htpasswd.matches(users.get(user), password);
    }

    @Cacheable(lifetime = Htpasswd.LIFETIME)
    private ConcurrentMap<String, String> fetch() {
        final ConcurrentMap<String, String> map =
            new ConcurrentHashMap<>(0);
        final Iterable<String> lines = Splitter.on('\n').split(this.content());
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
            content = baos.toString(StandardCharsets.UTF_8).trim();
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
     * @since 0.0.1
     */
    @FunctionalInterface
    interface Algorithm {

        /**
         * Do they match?
         * @param hash The hash
         * @param password The password
         * @return TRUE if they match
         */
        boolean matches(String hash, String password);
    }
}
