/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import com.s3auth.hosts.Stats;

/**
 * Dummy host stats.
 * @since 0.0.1
 */
final class DummyStats implements Stats {

    @Override
    public long bytesTransferred() {
        return 0;
    }
}
