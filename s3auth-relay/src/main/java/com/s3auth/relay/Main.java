/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.s3auth.hosts.DynamoHosts;
import java.util.concurrent.TimeUnit;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Main entrance to the system.
 *
 * @since 0.0.1
 */
@Immutable
@ToString
@EqualsAndHashCode
@Loggable(value = Loggable.INFO, limit = Integer.MAX_VALUE)
public final class Main {

    /**
     * It's a utility class.
     */
    private Main() {
        // intentionally empty
    }

    /**
     * Entrance.
     * @param args Optional arguments
     * @throws Exception If something is wrong
     * @todo #213:30min Create a FtpFacade in order to provide a FTP gateway.
     *  Also unignore test 'connectDisconnect' in FtpFacadeTest.
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static void main(final String[] args) throws Exception {
        final OptionParser parser = new OptionParser("p:s:d");
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
        final HttpFacade facade =
            new HttpFacade(new DynamoHosts(), port, secured);
        facade.listen();
        Logger.warn(Main.class, "started at http://localhost:%d...", port);
        if (options.has("d")) {
            while (true) {
                TimeUnit.MINUTES.sleep(1);
            }
        }
    }

}
