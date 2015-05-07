/**
 * Copyright (c) 2012-2015, s3auth.com
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
import com.s3auth.hosts.Hosts;
import java.io.IOException;
import java.net.ServerSocket;
import javax.validation.constraints.NotNull;
import lombok.ToString;

/**
 * FTP facade (port listener).
 *
 * <p>The class is instantiated in {@link Main}, once per application run.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Simon Njenga (simtuje@gmail.com)
 * @author Felipe Pina (felipe.pina@gmail.com)
 * @version $Id$
 * @since 0.0.1
 * @see Main
 * @todo #213:30min Implement TLS secure port listening in a manner analogous to
 *  HttpFacade.
 */
@ToString
@SuppressWarnings("PMD.DoNotUseThreads")
@Loggable(Loggable.DEBUG)
final class FtpFacade extends BaseFacade {

    /**
     * Constructor.
     * @param hosts Hosts
     * @param port Port number
     * @throws IOException If can't initialize
     */
    FtpFacade(@NotNull final Hosts hosts, final int port)
        throws IOException {
        super(2, "FTP-front", THREADS, "FTP-back", new ServerSocket(port),
            null);
        super.executeDispatch(false, hosts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void listen() {
        super.listen(false);
    }
}
