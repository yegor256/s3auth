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

import com.jcabi.aspects.Loggable;
import com.s3auth.hosts.Range;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * HTTP request.
 *
 * <p>This class helps us to consume a HTTP request from an IO socket, and
 * parse its content. This is how it can be used (socket should be opened):
 *
 * <pre>
 * HttpRequest req = new HttpRequest(socket);
 * String type = req.headers().get("Accept").iterator().next();
 * URI uri = req.requestUri();
 * </pre>
 *
 * <p>We don't support any other methods except "GET".
 *
 * <p>The class is mutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 * @see HttpThread
 */
@ToString
@EqualsAndHashCode(of = { "mtd", "uri", "hdrs" })
@Loggable(Loggable.DEBUG)
final class HttpRequest {

    /**
     * Range HTTP header.
     * @see <a href="HTTP headers">http://en.wikipedia.org/wiki/List_of_HTTP_header_fields</a>
     */
    private static final String RANGE_HEADER = "Range";

    /**
     * Range header matching pattern.
     */
    private static final Pattern RANGE_PATTERN =
        Pattern.compile("bytes=(\\d+)-(\\d+)");

    /**
     * TOP line pattern.
     */
    private static final Pattern TOP =
        Pattern.compile("(GET|POST|PUT|OPTIONS) (/[^ ]*) HTTP/1\\.(0|1)");

    /**
     * Header line pattern.
     */
    private static final Pattern HEADER =
        Pattern.compile("([a-zA-Z][a-zA-Z\\-]*):\\s*(.*)\\s*");

    /**
     * HTTP mtd.
     */
    private final transient String mtd;

    /**
     * URI requested.
     */
    private final transient URI uri;

    /**
     * HTTP headers.
     */
    private final transient ConcurrentMap<String, Collection<String>> hdrs =
        new ConcurrentHashMap<String, Collection<String>>();

    /**
     * Public ctor.
     *
     * <p>It's important NOT to close reader in this mtd. If it's closed
     * here the entire socket gets closed.
     *
     * @param socket Socket to read from
     * @throws IOException If some socket problem
     * @see <a href="http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol">HTTP</a>
     */
    HttpRequest(@NotNull final Socket socket) throws IOException {
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );
        final String top = reader.readLine();
        if (top == null) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                "empty request"
            );
        }
        final Matcher matcher = HttpRequest.TOP.matcher(top);
        if (!matcher.matches()) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                String.format("invalid first line: '%s'", top)
            );
        }
        if (!"GET".equals(matcher.group(1))) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_METHOD,
                "only GET mtd is supported"
            );
        }
        this.mtd = matcher.group(1);
        this.uri = URI.create(matcher.group(2));
        final Collection<String> headers = new LinkedList<String>();
        while (true) {
            final String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.isEmpty()) {
                break;
            }
            headers.add(line);
        }
        this.hdrs.putAll(this.parse(headers));
    }

    /**
     * Get all found HTTP headers.
     * @return Headers
     */
    public ConcurrentMap<String, Collection<String>> headers() {
        return this.hdrs;
    }

    /**
     * Get URI requested.
     * @return The URI
     */
    public URI requestUri() {
        return this.uri;
    }

    /**
     * Get HTTP method requested.
     * @return The method
     */
    public String method() {
        return this.mtd;
    }

    /**
     * Get range requested.
     * @return The URI
     * @throws HttpException If something is wrong
     * @see <a href="http://en.wikipedia.org/wiki/Byte_serving">Byte Serving</a>
     */
    public Range range() throws HttpException {
        final Range range;
        if (this.hdrs.containsKey(HttpRequest.RANGE_HEADER)) {
            final Matcher matcher = HttpRequest.RANGE_PATTERN.matcher(
                this.hdrs.get(HttpRequest.RANGE_HEADER).iterator().next()
            );
            if (!matcher.matches()) {
                throw new HttpException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    "invalid Range header format"
                );
            }
            range = new Range.Simple(
                Long.parseLong(matcher.group(1)),
                Long.parseLong(matcher.group(2))
            );
        } else {
            range = Range.ENTIRE;
        }
        return range;
    }

    /**
     * Parse header lines and create full map.
     * @param lines All lines
     * @return Map of headers
     * @throws HttpException If some socket problem
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private ConcurrentMap<String, Collection<String>> parse(
        final Iterable<String> lines) throws HttpException {
        final ConcurrentMap<String, Collection<String>> map =
            new ConcurrentHashMap<String, Collection<String>>();
        for (final String line : lines) {
            final Matcher matcher = HttpRequest.HEADER.matcher(line);
            if (!matcher.matches()) {
                throw new HttpException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    String.format("invalid header line: '%s'", line)
                );
            }
            final String name = HttpRequest.normalized(
                matcher.group(1).trim().toLowerCase(Locale.ENGLISH)
            );
            map.putIfAbsent(name, new LinkedList<String>());
            map.get(name).add(matcher.group(2));
        }
        return map;
    }

    /**
     * Normalize the key by converting to following form: first char
     * upper case, rest lower case, key is presumed to be ASCII).
     * @param key The HTTP header key
     * @return Normalized value
     * @see <a href="http://www.java2s.com/Open-Source/Java/6.0-JDK-Modules/jax-ws-runtime/com/sun/xml/ws/transport/Headers.java.htm">stolen from here</a>
     */
    private static String normalized(final String key) {
        final int len = key.length();
        final char[] bytes = key.toCharArray();
        if (bytes[0] >= 'a' && bytes[0] <= 'z') {
            bytes[0] -= 'a' - 'A';
        }
        for (int pos = 1; pos < len; ++pos) {
            if (bytes[pos] >= 'A' && bytes[pos] <= 'Z') {
                bytes[pos] += 'a' - 'A';
            }
        }
        return new String(bytes);
    }

}
