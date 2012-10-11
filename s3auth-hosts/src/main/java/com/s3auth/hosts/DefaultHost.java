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
import javax.validation.constraints.NotNull;

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
        Resource resource;
        try {
            resource = new DefaultResource(
                this.bucket.client().getObject(
                    new GetObjectRequest(
                        this.bucket.name(),
                        this.object(uri)
                    )
                )
            );
        } catch (com.amazonaws.AmazonClientException ex) {
            throw new IOException(
                String.format(
                    "failed to fetch '%s' from '%s' (key=%s), %s",
                    uri,
                    this.bucket.name(),
                    this.bucket.key(),
                    ex.getMessage()
                ),
                ex
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
    public boolean authorized(@NotNull final String user,
        @NotNull final String password)
        throws IOException {
        boolean auth;
        if (user.equals(this.bucket.key())
            && password.equals(this.bucket.secret())) {
            auth = true;
        } else {
            auth = this.htpasswd.authorized(user, password);
        }
        Logger.debug(this, "#authorized('%s', '%s'): %B", user, password, auth);
        return auth;
    }

    /**
     * Convert URI to S3 object name.
     * @param uri The URI
     * @return Object name
     */
    private String object(final URI uri) {
        String name = uri.getPath();
        if (name.isEmpty() || "/".equals(name)) {
            name = "/index.html";
        }
        if (name.charAt(0) == '/') {
            name = name.substring(1);
        }
        return name;
    }

}
