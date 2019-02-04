/**
 * Copyright (c) 2012-2019, s3auth.com
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
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
