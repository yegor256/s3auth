/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import javax.validation.constraints.NotNull;

/**
 * Exception during FTP command processing.
 *
 * <p>It carries the FTP reply code and text, which should be sent to
 * the client when a command can't be processed. The class is
 * instantiated by {@link FtpThread}.</p>
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @see FtpThread
 * @since 0.0.1
 */
@Loggable(Loggable.DEBUG)
final class FtpException extends IOException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7529FA789ED21565L;

    /**
     * FTP reply code.
     */
    private final transient int code;

    /**
     * FTP reply text.
     */
    private final transient String text;

    /**
     * Public ctor.
     *
     * @param cde The FTP reply code
     * @param txt The FTP reply text
     */
    FtpException(final int cde, @NotNull final String txt) {
        this(cde, txt, null);
    }

    /**
     * Public ctor.
     *
     * @param cde The FTP reply code
     * @param txt The FTP reply text
     * @param cause The cause of it
     */
    FtpException(final int cde, @NotNull final String txt,
        final Throwable cause) {
        super(cause);
        this.code = cde;
        this.text = txt;
    }

    /**
     * Build FTP response.
     *
     * @return The response
     */
    FtpResponse response() {
        return new FtpResponse().withCode(this.code).withText(this.text);
    }
}
