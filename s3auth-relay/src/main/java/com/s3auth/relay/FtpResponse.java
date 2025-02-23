/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Loggable;
import java.io.IOException;
import java.net.Socket;
import javax.validation.constraints.NotNull;

/**
 * FTP response, writable to IO socket.
 *
 * <p>It is a Builder design pattern, which can be used as the following:
 *
 * <pre> new FtpResponse()
 *   .withCode(230)
 *   .withText("here is my text")
 *   .send(socket);</pre>
 *
 * <p>By default FTP code is OK (200) and content is empty.
 *
 * <p>The class is NOT thread-safe.
 *
 * @since 0.0.1
 * @see FtpThread
 * @todo #213:30min Implement methods 'withCode', 'withText', 'send' as above.
 *  Also, implement 'asString' and unignore test 'constructsCorrectly' in
 *  FtpResponseTest.
 */
@Loggable(Loggable.DEBUG)
final class FtpResponse {

    /**
     * With this FTP code.
     * @param cde The FTP reply code to set
     * @return This object
     */
    public FtpResponse withCode(final int cde) {
        return this;
    }

    /**
     * With this FTP text.
     * @param text Text of the reply
     * @return This object
     */
    public FtpResponse withText(@NotNull final String text) {
        return this;
    }

    /**
     * Renders this FTP response as a String.
     * @return The response's canonical String representation ($code $text)
     */
    public String asString() {
        return this.toString();
    }

    @Override
    public String toString() {
        return this.asString();
    }

    /**
     * Send it to the socket.
     * @param socket The socket to write to
     * @return How many bytes were actually sent
     * @checkstyle NonStaticMethodCheck (10 lines)
     */
    @Loggable(
        value = Loggable.DEBUG, limit = Integer.MAX_VALUE,
        ignore = IOException.class
    )
    public long send(@NotNull final Socket socket) {
        return 0L;
    }

}
