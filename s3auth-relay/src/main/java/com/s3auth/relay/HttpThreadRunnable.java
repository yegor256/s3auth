/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.jcabi.log.Logger;

/**
 * Dispatcher of HttpThread.
 * @since 0.0.1
 */
final class HttpThreadRunnable implements Runnable {

    /**
     * The thread to run.
     */
    private final transient HttpThread thread;

    /**
     * Constructor.
     * @param thrd The HttpThread
     */
    HttpThreadRunnable(final HttpThread thrd) {
        this.thread = thrd;
    }

    @Override
    public void run() {
        try {
            this.thread.dispatch();
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            Logger.warn(this, "%s", ex);
        }
    }
}
