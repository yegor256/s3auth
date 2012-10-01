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
package com.s3auth.rest;

import com.rexsl.misc.CookieBuilder;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.CharEncoding;

/**
 * Flash cookie.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class FlashCookie extends NewCookie {

    /**
     * Colors available.
     */
    public enum Color {
        /**
         * Green.
         */
        GREEN,
        /**
         * Red.
         */
        RED;
    }

    /**
     * Name of flash cookie.
     */
    public static final String NAME = "s3auth-flash";

    /**
     * The message.
     */
    private final transient String msg;

    /**
     * The color of it (red, green, etc).
     */
    private final transient Color clr;

    /**
     * Public ctor, from a cookie encoded text.
     * @param text The text
     */
    public FlashCookie(@NotNull final String text) {
        super(FlashCookie.NAME, text);
        String[] parts;
        try {
            parts = new String(
                new Base32().decode(text), CharEncoding.UTF_8
            ).split(":", 2);
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        if (!parts[0].matches("GREEN|RED")) {
            throw new IllegalArgumentException(
                String.format("invalid cookie '%s'", text)
            );
        }
        this.clr = FlashCookie.Color.valueOf(parts[0]);
        this.msg = parts[1];
    }

    /**
     * Public ctor, from exact values.
     * @param base Base URI where we're using it
     * @param message The message
     * @param color The color
     */
    public FlashCookie(@NotNull final URI base, @NotNull final String message,
        @NotNull final Color color) {
        super(
            new CookieBuilder(base)
                .name(FlashCookie.NAME)
                .value(FlashCookie.encode(message, color))
                .temporary()
                .build()
        );
        this.msg = message;
        this.clr = color;
    }

    /**
     * Get message.
     * @return The message
     */
    public String message() {
        return this.msg;
    }

    /**
     * Get color of it.
     * @return The color
     */
    public Color color() {
        return this.clr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        return "/";
    }

    /**
     * Throw an exception that will forward to the page with an error message.
     * @param uri The URI to forward to
     * @param message The message to show as error
     * @return The exception to throw
     */
    public static WebApplicationException forward(@NotNull final URI uri,
        @NotNull final String message) {
        return new WebApplicationException(
            Response.status(HttpURLConnection.HTTP_SEE_OTHER)
                .location(uri)
                .cookie(new FlashCookie(uri, message, FlashCookie.Color.RED))
                .header("s3auth-error", message)
                .entity(message)
                .build()
        );
    }

    /**
     * Throw an exception that will forward to the page with an error message.
     * @param uri The URI to forward to
     * @param cause The cause of this problem
     * @return The exception to throw
     */
    public static WebApplicationException forward(@NotNull final URI uri,
        @NotNull final Exception cause) {
        return FlashCookie.forward(uri, cause.getMessage());
    }

    /**
     * Remove us from the builder.
     * @param builder The builder
     * @param base Base URI
     */
    public void clean(@NotNull final Response.ResponseBuilder builder,
        @NotNull final URI base) {
        builder.cookie(new CookieBuilder(base).name(FlashCookie.NAME).build());
    }

    /**
     * Encode message and color.
     * @param message The message
     * @param color The color
     * @return Encoded cookie value
     */
    private static String encode(final String message, final Color color) {
        try {
            return new Base32().encodeToString(
                String.format("%s:%s", color, message)
                    .getBytes(CharEncoding.UTF_8)
            );
        } catch (java.io.UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
