/*
 * Copyright (c) 2012-2025, Yegor Bugayenko
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

import com.s3auth.hosts.Hosts;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Single FTP processing thread.
 *
 * <p>The class is responsible for getting a new socket from a blocking
 * queue, processing it, and closing the socket. The class is instantiated
 * by {@link FtpFacade} and is executed by Services Executor routinely.
 *
 * <p>The class is thread-safe.
 *
 * @since 0.0.1
 * @see FtpFacade
 * @todo #213:30min Implement authentication based on the USER and PASS
 *  commands.
 * @todo #213:30min Implement relay functionality to fetch resource upon
 *  receival of the RETR command with valid authorization. Return an error
 *  in case of other commands (unsupported for now). Remove unnecessary asserts
 *  ffor fields 'sockets' and 'hosts' in method 'dispatch'.
 */
@ToString
@EqualsAndHashCode(of = { "hosts", "sockets" })
final class FtpThread {

    /**
     * Queue of sockets to get from.
     */
    @NotNull
    private final transient BlockingQueue<Socket> sockets;

    /**
     * Hosts to work with.
     */
    @NotNull
    private final transient Hosts hosts;

    /**
     * Public ctor.
     * @param sckts Sockets to read from
     * @param hsts Hosts
     */
    FtpThread(@NotNull final BlockingQueue<Socket> sckts,
        @NotNull final Hosts hsts) {
        this.sockets = sckts;
        this.hosts = hsts;
    }

    /**
     * Dispatch one request from the encapsulated queue.
     * @return Amount of bytes sent to socket
     */
    public long dispatch() {
        assert this.hosts != null;
        assert this.sockets != null;
        return 0L;
    }

}
