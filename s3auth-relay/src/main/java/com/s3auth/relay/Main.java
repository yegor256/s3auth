/*
 * Copyright (c) 2012-2024, Yegor Bugayenko
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
