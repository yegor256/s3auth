/**
 * Copyright (c) 2012, s3auth.com
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

import com.jcabi.log.Logger;
import com.s3auth.hosts.DynamoHosts;
import java.util.concurrent.TimeUnit;

/**
 * Main entrance to the system.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
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
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException(
                "one argument with port number required"
            );
        }
        final int port = Integer.valueOf(args[0]);
        final HttpFacade facade = new HttpFacade(new DynamoHosts(), port);
        facade.listen();
        Logger.info(Main.class, "started at http://localhost:%d...", port);
        while (true) {
            TimeUnit.MINUTES.sleep(1);
        }
    }

}
