/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth;

import com.s3auth.hosts.DynamoHosts;
import com.s3auth.hosts.SyslogHosts;
import com.s3auth.rest.TkApp;
import java.io.IOException;
import org.takes.http.Exit;
import org.takes.http.FtCli;

/**
 * Launch (used only for heroku).
 * @since 0.0.1
 */
public final class Launch {

    /**
     * Utility class.
     */
    private Launch() {
        // intentionally empty
    }

    /**
     * Entry point.
     * @param args Command line args
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static void main(final String[] args) throws IOException {
        new FtCli(
            new TkApp(new SyslogHosts(new DynamoHosts())),
            args
        ).start(Exit.NEVER);
    }

}
