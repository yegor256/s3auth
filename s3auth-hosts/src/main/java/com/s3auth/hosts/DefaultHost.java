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

import com.amazonaws.services.s3.model.GetObjectRequest;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.StringUtils;

/**
 * Default implementation of {@link Host}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
final class DefaultHost implements Host {

    /**
     * The S3 bucket.
     */
    private final transient Bucket bucket;

    /**
     * Htpasswd file abstraction.
     */
    private final transient Htpasswd htpasswd;

    /**
     * Public ctor.
     * @param bckt The S3 bucket to use
     */
    public DefaultHost(@NotNull final Bucket bckt) {
        this.bucket = bckt;
        this.htpasswd = new Htpasswd(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("%s with %s", this.bucket, this.htpasswd);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Resource fetch(@NotNull final URI uri) throws IOException {
        Resource resource = null;
        final Iterator<String> names = this.objects(uri);
        final Collection<String> errors = new LinkedList<String>();
        while (names.hasNext()) {
            final String name = names.next();
            try {
                resource = new DefaultResource(
                    this.bucket.client().getObject(
                        new GetObjectRequest(
                            this.bucket.name(),
                            name
                        )
                    )
                );
                break;
            } catch (com.amazonaws.AmazonClientException ex) {
                errors.add(
                    String.format(
                        "'%s': %s",
                        name,
                        ex.getMessage()
                    )
                );
            }
        }
        if (resource == null) {
            throw new IOException(
                Logger.format(
                    "failed to fetch %s from '%s' (key=%s): %[list]s",
                    uri,
                    this.bucket.name(),
                    this.bucket.key(),
                    errors
                )
            );
        }
        Logger.debug(
            this,
            "#fetch('%s'): found at %s",
            uri,
            this.bucket.name()
        );
        return resource;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHidden(@NotNull final URI uri) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authorized(@NotNull final String user,
        @NotNull final String password) throws IOException {
        boolean auth;
        if (user.equals(this.bucket.key())
            && password.equals(this.bucket.secret())) {
            auth = true;
        } else {
            auth = this.htpasswd.authorized(user, password);
        }
        Logger.debug(
            this,
            "#authorized('%s', '%s'): %B",
            user,
            password,
            auth
        );
        return auth;
    }

    /**
     * Name generator.
     */
    private interface Name {
        /**
         * Returns a name of S3 object.
         * @return The name
         */
        String get();
    }

    /**
     * Convert URI to all possible S3 object names (in order of importance).
     * @param uri The URI
     * @return Object names
     */
    private Iterator<String> objects(final URI uri) {
        final String name = StringUtils.strip(uri.getPath(), "/");
        final Collection<Name> names = new LinkedList<Name>();
        if (!name.isEmpty()) {
            names.add(
                new Name() {
                    @Override
                    public String get() {
                        return name;
                    }
                }
            );
        }
        final Iterator<Name> origin = names.iterator();
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return origin.hasNext();
            }
            @Override
            public String next() {
                return origin.next().get();
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
