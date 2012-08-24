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

import com.rexsl.core.Manifests;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.io.IOUtils;

/**
 * HTTP response.
 *
 * <p>The class is NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
final class HttpResponse {

    /**
     * Name of the server we show in HTTP headers.
     */
    private static final String NAME = String.format(
        "s3auth.com, %s/%s built on %s",
        Manifests.read("S3Auth-Version"),
        Manifests.read("S3Auth-Revision"),
        Manifests.read("S3Auth-Date")
    );

    /**
     * Status.
     */
    private transient int status = HttpURLConnection.HTTP_OK;

    /**
     * HTTP headers.
     */
    private final transient ConcurrentMap<String, Collection<String>> headers =
        new ConcurrentHashMap<String, Collection<String>>();

    /**
     * Body.
     */
    private transient InputStream body;

    /**
     * Public ctor.
     */
    public HttpResponse() {
        this.withHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        this.withHeader(HttpHeaders.EXPIRES, "-1");
        this.withHeader(
            HttpHeaders.DATE,
            String.format("%ta, %1$td %1$tb %1$tY %1$tT %1$tz", new Date())
        );
        this.withHeader("Server", HttpResponse.NAME);
    }

    /**
     * Set HTTP status.
     * @param stts The HTTP status to set
     * @return This object
     */
    public HttpResponse withStatus(final int stts) {
        this.status = stts;
        return this;
    }

    /**
     * Add HTTP header.
     * @param name Name of the HTTP header
     * @param value Text value
     * @return This object
     */
    public HttpResponse withHeader(final String name,
        final String value) {
        this.headers.putIfAbsent(name, new LinkedList<String>());
        this.headers.get(name).add(value);
        return this;
    }

    /**
     * With this HTTP body.
     * @param stream The stream to get the body from
     * @return This object
     */
    public HttpResponse withBody(final InputStream stream) {
        this.body = stream;
        return this;
    }

    /**
     * With this HTTP body.
     * @param text Text of the body
     * @return This object
     */
    public HttpResponse withBody(final String text) {
        this.body = IOUtils.toInputStream(text);
        return this;
    }

    /**
     * Send it to the socket.
     * @param socket The socket to write to
     * @return How many bytes were actually sent
     * @throws IOException If some IO problem inside
     */
    public int send(final Socket socket) throws IOException {
        final OutputStream stream = socket.getOutputStream();
        final Writer writer = new OutputStreamWriter(stream);
        writer.write(String.format("HTTP/1.1 %d %s\n", this.status, "empty"));
        for (ConcurrentMap.Entry<String, Collection<String>> hdr
            : this.headers.entrySet()) {
            for (String value : hdr.getValue()) {
                writer.write(hdr.getKey());
                writer.write(": ");
                writer.write(value);
                // @checkstyle MultipleStringLiterals (1 line)
                writer.write("\n");
            }
        }
        writer.write("\n");
        writer.flush();
        int bytes = 0;
        if (this.body != null) {
            bytes = IOUtils.copy(this.body, stream);
        }
        writer.close();
        return bytes;
    }

}
