/**
 * Copyright (c) 2012-2022, Yegor Bugayenko
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
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * Collection of hosts.
 *
 * <p>Implementation must be thread-safe.
 *
 * @author Yegor Bugayenko (yegor256@gmail.com)
 * @version $Id$
 * @since 0.0.1
 */
@Immutable
public interface Hosts extends Closeable {

    /**
     * Find one host by domain name.
     * @param domain The domain name
     * @return Host found
     * @throws IOException If not found or some other IO problem
     */
    Host find(String domain) throws IOException;

    /**
     * Get domains of the given user.
     * @param user The user
     * @return Modifiable collection of domains
     * @throws IOException If some error inside
     */
    Set<Domain> domains(User user) throws IOException;

    /**
     * Thrown by {@link #find(String)} if domain is not found.
     */
    class NotFoundException extends IOException {
        /**
         * Serialization marker.
         */
        private static final long serialVersionUID = 0x7529FA789ED21479L;
        /**
         * Public ctor.
         * @param cause The cause of it
         */
        public NotFoundException(final String cause) {
            super(cause);
        }
    }

}
