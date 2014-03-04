/**
 * Copyright (c) 2012, s3auth.com
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
package com.s3auth.hosts;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.LogExceptions;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Crypt;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.io.Charsets;

/**
 * Htpasswd file abstraction.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@Loggable(Loggable.DEBUG)
@EqualsAndHashCode(of = "host")
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
    @Cacheable(lifetime = Htpasswd.LIFETIME, unit = TimeUnit.MINUTES)
    private ConcurrentMap<String, String> fetch() {
        final ConcurrentMap<String, String> map =
            new ConcurrentHashMap<String, String>(0);
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
                Range.ENTIRE
            );
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            res.writeTo(baos);
            content = baos.toString(Charsets.UTF_8.name()).trim();
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
     * @throws IOException If some error inside
     */
    private static boolean matches(final String hash, final String password)
        throws IOException {
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
     */
    private interface Algorithm {
        /**
         * Do they match?
         * @param hash The hash
         * @param password The password
         * @return TRUE if they match
         * @throws IOException If some error inside
         */
        boolean matches(String hash, String password) throws IOException;
    }

    /**
     * MD5 hash builder.
     */
    @Loggable(Loggable.DEBUG)
    private static final class Md5 implements Htpasswd.Algorithm {
        /**
         * MD5 pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("\\$apr1\\$([^\\$]+)\\$([a-zA-Z0-9/\\.]+=*)");

        @Override
        public boolean matches(final String hash, final String password)
            throws IOException {
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
     */
    @Loggable(Loggable.DEBUG)
    private static final class PlainText implements Htpasswd.Algorithm {
        @Override
        public boolean matches(final String hash, final String password) {
            return password.equals(hash);
        }
    }

}
