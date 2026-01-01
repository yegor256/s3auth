/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import org.apache.commons.net.ftp.FTPReply;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link com.s3auth.relay.FtpResponse}.
 *
 * @since 0.0.1
 */
final class FtpResponseTest {

    /**
     * FtpResponse can construct correct FTP response.
     */
    @Test
    @Disabled
    void constructsCorrectly() {
        MatcherAssert.assertThat(
            new FtpResponse()
                .withCode(FTPReply.COMMAND_OK)
                .withText("hi!")
                .asString(),
            Matchers.equalTo("200 hi!")
        );
    }

}
