/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.google.common.collect.Lists;
import com.jcabi.aspects.Loggable;
import com.s3auth.hosts.Range;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;

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
 * @since 0.0.1
 * @see HttpThread
 */
@ToString
@EqualsAndHashCode(of = { "mtd", "uri", "hdrs" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.UseConcurrentHashMap")
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
        Pattern.compile("bytes=(\\d+)-(\\d+)?");

    /**
     * TOP line pattern.
     */
    private static final Pattern TOP =
        Pattern.compile("(GET|POST|PUT|OPTIONS|HEAD) (/[^ ]*) HTTP/1\\.(0|1)");

    /**
     * Header line pattern.
     */
    private static final Pattern HEADER =
        Pattern.compile("([a-zA-Z][a-zA-Z\\-]*):\\s*(.*)\\s*");

    /**
     * Query param pattern.
     */
    private static final Pattern PARAMS =
        Pattern.compile("[\\?&]([a-zA-Z0-9][a-zA-Z0-9\\-]*)(=([a-zA-Z0-9]+))?");

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
    private final transient Map<String, Collection<String>> hdrs;

    /**
     * HTTP query params.
     */
    private final transient Map<String, Collection<String>> parms;

    /**
     * Public ctor.
     *
     * <p>It's important NOT to close reader in this mtd. If it's closed
     * here the entire socket gets closed.
     *
     * @param socket Socket to read from
     * @throws IOException If some socket problem
     * @see <a href="https://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol">HTTP</a>
     * @checkstyle ExecutableStatementCountCheck (100 lines)
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    HttpRequest(@NotNull final Socket socket) throws IOException {
        final BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)
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
        this.parms = Collections.unmodifiableMap(HttpRequest.parseParameters(top));
        final String method = matcher.group(1);
        if (!"GET".equals(method) && !"HEAD".equals(method)) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_METHOD,
                "only GET or HEAD methods are supported"
            );
        }
        this.mtd = matcher.group(1);
        try {
            this.uri = new URI(matcher.group(2));
        } catch (final URISyntaxException ex) {
            throw new HttpException(
                HttpURLConnection.HTTP_BAD_REQUEST,
                String.format(
                    "request URI is not encoded correctly: %s",
                    ex
                )
            );
        }
        final Collection<String> headers = new LinkedList<>();
        while (true) {
            final String line = reader.readLine();
            if (StringUtils.isEmpty(line)) {
                break;
            }
            headers.add(line);
        }
        this.hdrs = Collections.unmodifiableMap(HttpRequest.parseHeaders(headers));
    }

    /**
     * Get all found HTTP headers. Note that the returned map is unmodifiable.
     * @return Headers
     */
    public Map<String, Collection<String>> headers() {
        return Collections.unmodifiableMap(this.hdrs);
    }

    /**
     * Get all found HTTP parameters.
     * Note that the returned map is unmodifiable.
     * @return Headers
     */
    public Map<String, Collection<String>> parameters() {
        return Collections.unmodifiableMap(this.parms);
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
            final long last;
            if (matcher.group(2) == null) {
                last = Long.MAX_VALUE;
            } else {
                last = Long.parseLong(matcher.group(2));
            }
            range = new Range.Simple(Long.parseLong(matcher.group(1)), last);
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
    private static Map<String, Collection<String>> parseHeaders(
        final Iterable<String> lines) throws HttpException {
        final Map<String, Collection<String>> map =
            new CaseInsensitiveMap<>();
        for (final String line : lines) {
            final Matcher matcher = HttpRequest.HEADER.matcher(line);
            if (!matcher.matches()) {
                throw new HttpException(
                    HttpURLConnection.HTTP_BAD_REQUEST,
                    String.format("invalid header line: '%s'", line)
                );
            }
            final String name =
                matcher.group(1).trim().toLowerCase(Locale.ENGLISH);
            if (!map.containsKey(name)) {
                map.put(name, Lists.newLinkedList());
            }
            map.get(name).add(matcher.group(2));
        }
        return map;
    }

    /**
     * Parse query parameters from request string and create full map.
     * @param request Request string
     * @return Map of headers
     */
    private static Map<String, Collection<String>> parseParameters(
        final CharSequence request) {
        final Map<String, Collection<String>> map =
            new HashMap<>(0);
        final Matcher matcher = HttpRequest.PARAMS.matcher(request);
        while (matcher.find()) {
            final String name = matcher.group(1).trim();
            if (!map.containsKey(name)) {
                map.put(name, Lists.newLinkedList());
            }
            map.get(name).add(
                StringUtils.defaultString(matcher.group(3))
            );
        }
        return map;
    }

}
