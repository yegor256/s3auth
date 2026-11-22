/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.s3auth.hosts.DynamoHosts;
import com.s3auth.hosts.Hosts;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * Main entrance to the system.
 *
 * @since 0.0.1
 */
@Immutable
@Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
public final class Main {

    /**
     * Highest valid TCP port number.
     */
    private static final int MAX_PORT = 0xFFFF;

    /**
     * It's a utility class.
     */
    private Main() {
        // intentionally empty
    }

    /**
     * Entrance.
     *
     * @param args Optional arguments
     * @throws Exception If something is wrong
     */
    public static void main(final String[] args) throws Exception {
        Main.main(args, new DynamoHosts());
    }

    /**
     * Entrance with injectable hosts, to be used in tests.
     *
     * @param args Optional arguments
     * @param hosts Hosts to serve requests from
     * @throws Exception If something is wrong
     */
    static void main(final String[] args, final Hosts hosts) throws Exception {
        final OptionParser parser = new OptionParser("p:s:f:d");
        final OptionSet options = parser.parse(args);
        final int port;
        if (options.has("p")) {
            port = Integer.parseInt(options.valueOf("p").toString());
        } else {
            port = 80;
        }
        final int secured;
        if (options.has("s")) {
            secured = Integer.parseInt(options.valueOf("s").toString());
        } else {
            secured = 443;
        }
        final int ftp;
        if (options.has("f")) {
            ftp = Integer.parseInt(options.valueOf("f").toString());
            Main.validate(ftp);
        } else {
            ftp = -1;
        }
        final HttpFacade facade =
            HttpFacade.open(hosts, port, secured);
        try {
            facade.listen();
            Logger.warn(Main.class, "started at http://localhost:%d...", port);
            if (ftp >= 0) {
                final FtpFacade gateway = FtpFacade.open(hosts, ftp);
                gateway.listen();
                Logger.warn(
                    Main.class, "started at ftp://localhost:%d...", ftp
                );
            }
        } catch (final IOException ex) {
            Main.close(facade, ex);
            throw ex;
        }
        if (options.has("d")) {
            while (true) {
                TimeUnit.MINUTES.sleep(1);
            }
        }
    }

    private static void validate(final int port) {
        if (port < 0 || port > Main.MAX_PORT) {
            throw new IllegalArgumentException(
                String.format(
                    "Port %d is out of valid range [0..%d]",
                    port, Main.MAX_PORT
                )
            );
        }
    }

    private static void close(final Closeable facade, final Throwable failure) {
        try {
            facade.close();
        } catch (final IOException ex) {
            failure.addSuppressed(ex);
        }
    }
}
