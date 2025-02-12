/*
 * Copyright (c) 2012-2025, Yegor Bugayenko
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

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.jcabi.aspects.Immutable;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

/**
 * One host.
 *
 * @since 0.0.1
 */
@Immutable
public interface Host extends Closeable {

    /**
     * Find resource and return its input stream.
     * @param uri Name of resource
     * @param range Range of data to return
     * @param version The version of the data to return
     * @return The stream
     * @throws IOException If some error with I/O inside
     */
    Resource fetch(URI uri, Range range, Version version) throws IOException;

    /**
     * This URI require authentication?
     * @param uri Which URI we're trying to access
     * @return Yes or no
     * @throws IOException If some error with I/O inside
     */
    boolean isHidden(URI uri) throws IOException;

    /**
     * Can this user login in with this credentials?
     * @param user User name
     * @param password Password
     * @return Yes or no
     * @throws IOException If some error with I/O inside
     */
    boolean authorized(String user, String password) throws IOException;

    /**
     * Get this resource's syslog host and port.
     * @return Syslog host and port
     */
    String syslog();

    /**
     * Get the stats for this host's domain.
     * @return Statistics for this domain
     */
    Stats stats();

    /**
     * Client to Amazon CloudWatch.
     *
     * @since 0.0.1
     */
    @Immutable
    interface CloudWatch {
        /**
         * Get Amazon client.
         * @return The client
         */
        AmazonCloudWatch get();
    }

}
