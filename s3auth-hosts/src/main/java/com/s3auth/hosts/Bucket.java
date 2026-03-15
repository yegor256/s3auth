/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Single Amazon S3 bucket.
 *
 * @since 0.0.2
 */
@Immutable
public interface Bucket extends Domain {

    /**
     * Get amazon client.
     * @return The client
     */
    S3Client client();

}
