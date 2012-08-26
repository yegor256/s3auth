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

import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

/**
 * Htpasswd file abstraction.
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
final class Htpasswd {

    /**
     * How often to reload from the host, in milliseconds.
     */
    private static final int PERIOD_MS = 5 * 60 * 1000;

    /**
     * All known algorithms.
     * @see <a href="http://httpd.apache.org/docs/2.2/misc/password_encryptions.html">Algorithms supported by Apache</a>
     */
    private static final Htpasswd.Algorithm[] ALGORITHMS =
        new Htpasswd.Algorithm[] {
            new Htpasswd.Md5(),
            new Htpasswd.Sha(),
            new Htpasswd.Crypt(),
            new Htpasswd.PlainText(),
        };

    /**
     * The host we're working with.
     */
    private final transient Host host;

    /**
     * When was the file recently loaded from S3?
     */
    private final transient AtomicLong updated = new AtomicLong();

    /**
     * Map of user names and their hashes.
     */
    private final transient ConcurrentMap<String, String> map =
        new ConcurrentHashMap<String, String>();

    /**
     * Public ctor.
     * @param hst The host to work with
     */
    public Htpasswd(final Host hst) {
        this.host = hst;
    }

    /**
     * Can this user login in with this credentials?
     * @param user User name
     * @param password Password
     * @return Yes or no
     * @throws IOException If some error inside
     */
    public boolean authorized(final String user, final String password)
        throws IOException {
        final ConcurrentMap<String, String> users = this.fetch();
        return users.containsKey(user)
            && Htpasswd.matches(users.get(user), password);
    }

    /**
     * Get map of users and passwords from the host.
     * @return Map of users
     * @throws IOException If some error inside
     */
    private ConcurrentMap<String, String> fetch() throws IOException {
        synchronized (this.updated) {
            if (System.currentTimeMillis() - this.updated.get()
                > Htpasswd.PERIOD_MS) {
                this.map.clear();
                final String[] lines = IOUtils.toString(
                    this.host.fetch(URI.create("/.htpasswd"))
                ).trim().split("\n");
                for (String line : lines) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    final String[] parts = line.trim().split(":");
                    this.map.put(parts[0].trim(), parts[1].trim());
                }
                this.updated.set(System.currentTimeMillis());
            }
        }
        return this.map;
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
        for (Algorithm algo : Htpasswd.ALGORITHMS) {
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
    private static final class Md5 implements Htpasswd.Algorithm {
        /**
         * MD5 pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("\\$apr1\\$([^\\$]+)\\$([a-zA-Z0-9/\\.]+=*)");
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(final String hash, final String password)
            throws IOException {
            final Matcher matcher = Htpasswd.Md5.PATTERN.matcher(hash);
            boolean matches;
            if (matcher.matches()) {
                throw new IOException("MD5 hashes are not supported yet");
            } else {
                matches = false;
            }
            return matches;
        }
    }

    /**
     * SHA1 hash builder.
     */
    private static final class Sha implements Htpasswd.Algorithm {
        /**
         * SHA1 pattern.
         */
        private static final Pattern PATTERN =
            Pattern.compile("\\{SHA\\}([a-zA-Z0-9/]+=*)");
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(final String hash, final String password) {
            final Matcher matcher = Htpasswd.Sha.PATTERN.matcher(hash);
            boolean matches;
            if (matcher.matches()) {
                final String required = Base64.encodeBase64String(
                    DigestUtils.sha(password)
                );
                matches = matcher.group(1).equals(required);
                Logger.debug(
                    this,
                    "#matches('%s', '%s'): matches=%B, required='%s'",
                    hash,
                    password,
                    matches,
                    required
                );
            } else {
                matches = false;
            }
            return matches;
        }
    }

    /**
     * UNIX crypt.
     */
    private static final class Crypt implements Htpasswd.Algorithm {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(final String hash, final String password) {
            return false;
        }
    }

    /**
     * Plain Text.
     */
    private static final class PlainText implements Htpasswd.Algorithm {
        /**
         * {@inheritDoc}
         */
        @Override
        public boolean matches(final String hash, final String password) {
            return password.equals(hash);
        }
    }

}
