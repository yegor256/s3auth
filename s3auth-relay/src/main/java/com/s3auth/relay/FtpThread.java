/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
