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

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Default implementation of host.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class DefaultHost implements Host {

    /**
     * AWS client.
     */
    private final transient AmazonS3 client;

    /**
     * The domain.
     */
    private final transient Domain domain;

    /**
     * Public ctor.
     * @param dmn The domain
     */
    public DefaultHost(final Domain dmn) {
        this.domain = dmn;
        this.client = new AmazonS3Client(
            new BasicAWSCredentials(this.domain.key(), this.domain.secret())
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("host:%s", this.domain);
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
    public InputStream fetch(final URI uri) throws IOException {
        S3Object object;
        try {
            object = this.client.getObject(
                new GetObjectRequest(this.domain.name(), uri.toString())
            );
        } catch (com.amazonaws.AmazonClientException ex) {
            throw new IOException(ex);
        }
        final InputStream stream = object.getObjectContent();
        Logger.debug(
            this,
            "#fetch('%s'): found at %s",
            uri,
            this.domain.name()
        );
        return stream;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authorized(final String user, final String password) {
        boolean auth;
        if (user.equals(this.domain.key())
            && password.equals(this.domain.secret())) {
            auth = true;
        } else {
            auth = false;
        }
        Logger.debug(this, "#authorized('%s', '%s'): %B", user, password, auth);
        return auth;
    }

}
