/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
