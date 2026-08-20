/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.io.IOException;

/**
 * Custom IO exception.
 * @since 0.0.1
 */
final class StreamingException extends IOException {

    /**
     * Serialization marker.
     */
    private static final long serialVersionUID = 0x7529FA781E111179L;

    /**
     * Public ctor.
     * @param cause The cause of it
     * @param thr The cause of it
     */
    StreamingException(final String cause, final Throwable thr) {
        super(
            String.format("%s: '%s'", cause, thr.getMessage()),
            thr
        );
    }
}
