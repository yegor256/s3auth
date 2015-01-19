/**
 * Copyright (c) 2012-2015, s3auth.com
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

import com.jcabi.aspects.Loggable;
import com.s3auth.hosts.Resource;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.Charsets;

/**
 * HTTP response, writable to IO socket.
 *
 * <p>It is a Builder design pattern, which can be used as the following:
 *
 * <pre> new HttpResponse()
 *   .withStatus(200)
 *   .withHeader("Content-Type", "text/plain")
 *   .withHeader("Content-Length", "18")
 *   .withBody("here is my content")
 *   .send(socket);</pre>
 *
 * <p>By default HTTP status is OK (200) and content is empty.
 *
 * <p>The class is NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @see HttpThread
 */
@ToString
@EqualsAndHashCode(of = { "status", "hdrs", "body" })
@Loggable(Loggable.DEBUG)
final class HttpResponse {

    /**
     * EOL.
     */
    private static final String EOL = "\n";

    /**
     * Status.
     */
    private transient int status = HttpURLConnection.HTTP_OK;

    /**
     * HTTP headers.
     */
    private final transient ConcurrentMap<String, Collection<String>> hdrs =
        new ConcurrentHashMap<String, Collection<String>>(0);

    /**
     * Resource to deliver.
     */
    private transient Resource body = new Resource.PlainText("");

    /**
     * Set HTTP status.
     * @param stts The HTTP status to set
     * @return This object
     */
    public HttpResponse withStatus(final int stts) {
        if (stts < HttpURLConnection.HTTP_OK) {
            throw new IllegalArgumentException(
                String.format("illegal HTTP status %d", stts)
            );
        }
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
        @NotNull final String value) {
        this.hdrs.putIfAbsent(name, new LinkedList<String>());
        this.hdrs.get(name).add(value);
        return this;
    }

    /**
     * With this HTTP body.
     * @param res The resource to get the body from
     * @return This object
     */
    public HttpResponse withBody(@NotNull final Resource res) {
        this.body = res;
        this.withStatus(res.status());
        return this;
    }

    /**
     * With this HTTP body.
     * @param text Text of the body
     * @return This object
     */
    public HttpResponse withBody(@NotNull final String text) {
        this.body = new Resource.PlainText(text);
        return this;
    }

    /**
     * Send it to the socket.
     * @param socket The socket to write to
     * @return How many bytes were actually sent
     * @throws IOException If some IO problem inside
     * @see <a href="http://stackoverflow.com/questions/8179547">discussion</a>
     */
    @Loggable(
        value = Loggable.DEBUG, limit = Integer.MAX_VALUE,
        ignore = IOException.class
    )
    public long send(@NotNull final Socket socket) throws IOException {
        final OutputStream stream = socket.getOutputStream();
        final Writer writer = new OutputStreamWriter(stream, Charsets.UTF_8);
        try {
            writer.write(
                String.format(
                    "HTTP/1.1 %d %s%s",
                    this.status,
                    HttpStatus.getStatusText(this.status),
                    HttpResponse.EOL
                )
            );
            for (final ConcurrentMap.Entry<String, Collection<String>> hdr
                : this.hdrs.entrySet()) {
                for (final String value : hdr.getValue()) {
                    writer.write(hdr.getKey());
                    writer.write(": ");
                    writer.write(value);
                    writer.write(HttpResponse.EOL);
                }
            }
            for (final String hdr : this.body.headers()) {
                writer.write(hdr);
                writer.write(HttpResponse.EOL);
            }
            writer.write(HttpResponse.EOL);
            writer.flush();
            return this.body.writeTo(stream);
        } finally {
            writer.close();
        }
    }

}
