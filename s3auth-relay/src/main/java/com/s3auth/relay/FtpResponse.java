/*
 * Copyright (c) 2012-2022, Yegor Bugayenko
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
        return "";
    }

    @Override
    public String toString() {
        return this.asString();
    }

    /**
     * Send it to the socket.
     * @param socket The socket to write to
     * @return How many bytes were actually sent
     * @throws java.io.IOException If some IO problem inside
     */
    @Loggable(
        value = Loggable.DEBUG, limit = Integer.MAX_VALUE,
        ignore = IOException.class
    )
    public long send(@NotNull final Socket socket) throws IOException {
        return 0L;
    }

}
