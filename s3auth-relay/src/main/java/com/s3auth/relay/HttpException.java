/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Exception during HTTP request processing.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class HttpException extends IOException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7529FA789ED21459L;

    /**
     * HTTP response.
     */
    private final transient HttpResponse resp;

    /**
     * Public ctor.
     * @param status The status
     */
    HttpException(final int status) {
        this(status, "");
    }

    /**
     * Public ctor.
     * @param status The status
     * @param cause The cause of it
     */
    HttpException(final int status, @NotNull final String cause) {
        this(
            new HttpResponse()
                .withStatus(status)
                .withBody(cause)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
        );
    }

    /**
     * Public ctor.
     * @param status The status
     * @param cause The cause of it
     */
    HttpException(final int status, @NotNull final Throwable cause) {
        this(status, ExceptionUtils.getStackTrace(cause));
    }

    /**
     * Public ctor.
     * @param response The response
     */
    HttpException(@NotNull final HttpResponse response) {
        super(response.toString());
        this.resp = response;
    }

    /**
     * Build HTTP response.
     * @return The response
     */
    public HttpResponse response() {
        return this.resp;
    }

}
