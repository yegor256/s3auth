/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.amazonaws.services.s3.AmazonS3;
import com.jcabi.aspects.Immutable;

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
    AmazonS3 client();

}
