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
package com.s3auth.relay;

import com.jcabi.log.Logger;
import com.rexsl.core.Manifests;
import com.s3auth.hosts.Host;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import org.apache.commons.io.IOUtils;

/**
 * Local host with data.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
final class LocalHost implements Host {

    /**
     * Shutdown URL.
     */
    private static final String SHUTDOWN = String.format(
        "/shutdown/%s",
        Manifests.read("S3Auth-ExitKey")
    );

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    public InputStream fetch(final URI uri) throws IOException {
        String output;
        if ("/".equals(uri.toString())) {
            output = "see www.s3auth.com";
        } else if ("/version".equals(uri.toString())) {
            output = Manifests.read("S3Auth-Revision");
        } else if (uri.toString().equals(LocalHost.SHUTDOWN)) {
            output = "";
            Logger.warn(this, "fetch(%s): shutting down..", uri);
            System.exit(0);
        } else {
            throw new HttpException(
                HttpURLConnection.HTTP_NOT_FOUND,
                String.format("URI '%s' not found here", uri)
            );
        }
        return IOUtils.toInputStream(output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean authorized(final String user, final String password) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        // nothing to do
    }

}
