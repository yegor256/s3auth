/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

/**
 * Dispatcher of FTPThread.
 * @since 0.0.1
 */
final class FtpThreadRunnable implements Runnable {

    /**
     * The thread to run.
     */
    private final transient FtpThread thread;

    /**
     * Constructor.
     * @param thrd The FTPThread
     */
    FtpThreadRunnable(final FtpThread thrd) {
        this.thread = thrd;
    }

    @Override
    public void run() {
        this.thread.dispatch();
    }
}
