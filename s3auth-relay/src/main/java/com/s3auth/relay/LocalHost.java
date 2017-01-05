/**
 * Copyright (c) 2012-2017, s3auth.com
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.Host;
import com.s3auth.hosts.Range;
import com.s3auth.hosts.Resource;
import com.s3auth.hosts.Stats;
import com.s3auth.hosts.Version;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Local host with data.
 *
 * <p>It's a specific implementation of {@link Host}, which processes requests
 * that should not go to Amazon S3, but should stay within this server. Mostly
 * for deployment automation purposes. The class is instantiated by
 * {@link HttpThread} according to the information in {@code "Host"}
 * HTTP header.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @see HttpThread
 */
@Immutable
@EqualsAndHashCode
@Loggable(Loggable.DEBUG)
final class LocalHost implements Host {

    /**
     * Shutdown URL.
     */
    private static final String SHUTDOWN = String.format(
        "/shutdown/%s",
        Manifests.read("S3Auth-ExitKey")
    );

    /**
     * Is it your name?
     * @param name The name of host, provided in "Host" HTTP header
     * @return TRUE if this is a localhost
     */
    public static boolean isIt(@NotNull final String name) {
        return "relay.s3auth.com".equals(name);
    }

    @Override
    public Resource fetch(@NotNull final URI uri, @NotNull final Range range,
        @NotNull final Version version) throws IOException {
        if (uri.toString().startsWith("/shutdown")) {
            throw this.halt(uri.toString());
        }
        final String output;
        if ("/".equals(uri.toString())) {
            output = "see www.s3auth.com";
        } else if ("/version".equals(uri.toString())) {
            output = Manifests.read("S3Auth-Revision");
        } else {
            throw new HttpException(
                HttpURLConnection.HTTP_NOT_FOUND,
                String.format("URI '%s' not found here", uri)
            );
        }
        return new Resource.PlainText(output);
    }

    @Override
    public boolean isHidden(@NotNull final URI uri) {
        return false;
    }

    @Override
    public boolean authorized(@NotNull final String user,
        @NotNull final String password) {
        return true;
    }

    @Override
    public void close() {
        // nothing to do
    }

    @Override
    public String syslog() {
        return "localhost:514";
    }

    @Override
    public String toString() {
        return "localhost";
    }

    @Override
    public Stats stats() {
        return new DummyStats();
    }

    /**
     * Shutdown.
     * @param uri URI just dispatched
     * @return The exception to throw
     */
    @SuppressWarnings("PMD.DoNotCallSystemExit")
    @SuppressFBWarnings("DM_EXIT")
    private IOException halt(final String uri) {
        if (uri.equals(LocalHost.SHUTDOWN)) {
            Logger.warn(this, "fetch(%s): shutting down..", uri);
            System.exit(0);
        }
        return new HttpException(
            HttpURLConnection.HTTP_NOT_FOUND,
            String.format(
                "shutdown key ends with '%s...'",
                LocalHost.SHUTDOWN.substring(
                    // @checkstyle MagicNumber (1 line)
                    LocalHost.SHUTDOWN.length() - 5
                )
            )
        );
    }

    /**
     * Dummy host stats.
     */
    private static final class DummyStats implements Stats {
        @Override
        public long bytesTransferred() {
            return 0;
        }
    }

}
