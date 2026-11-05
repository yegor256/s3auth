/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPReply;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link com.s3auth.relay.FtpResponse}.
 *
 * @since 0.0.1
 */
final class FtpResponseTest {

    /**
     * Expected FTP reply line, with the CRLF terminator required by the
     * FTP protocol (RFC 959).
     */
    private static final String REPLY = String.format(
        "200 hi!%c%c", '\r', '\n'
    );

    /**
     * FtpResponse can construct correct FTP response.
     */
    @Test
    void constructsCorrectly() {
        MatcherAssert.assertThat(
            new FtpResponse()
                .withCode(FTPReply.COMMAND_OK)
                .withText("hi!")
                .asString(),
            Matchers.equalTo("200 hi!")
        );
    }

    /**
     * FtpResponse can construct the default FTP response.
     */
    @Test
    void constructsDefaultResponse() {
        MatcherAssert.assertThat(
            new FtpResponse().asString(),
            Matchers.equalTo("200 ")
        );
    }

    /**
     * FtpResponse can send the FTP reply line to a socket.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void sendsReplyToSocket() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket writing = new Socket("localhost", server.getLocalPort());
            Socket reading = server.accept()
        ) {
            new FtpResponse()
                .withCode(FTPReply.COMMAND_OK)
                .withText("hi!")
                .send(writing);
            final byte[] received = new byte[FtpResponseTest.REPLY.length()];
            IOUtils.read(reading.getInputStream(), received);
            MatcherAssert.assertThat(
                new String(received, StandardCharsets.UTF_8),
                Matchers.equalTo(FtpResponseTest.REPLY)
            );
        }
    }

    /**
     * FtpResponse can return the number of bytes sent to the socket.
     *
     * @throws Exception If there is some problem inside
     */
    @Test
    void returnsNumberOfBytesSent() throws Exception {
        try (
            ServerSocket server = new ServerSocket(0);
            Socket writing = new Socket("localhost", server.getLocalPort())
        ) {
            MatcherAssert.assertThat(
                new FtpResponse()
                    .withCode(FTPReply.COMMAND_OK)
                    .withText("hi!")
                    .send(writing),
                Matchers.equalTo((long) FtpResponseTest.REPLY.length())
            );
        }
    }
}
