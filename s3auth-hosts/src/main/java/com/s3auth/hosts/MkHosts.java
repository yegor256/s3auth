/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.util.Collections;
import java.util.Set;

/**
 * Mock.
 * @since 0.0.1
 */
final class MkHosts implements Hosts {

    @Override
    public void close() {
        // do nothing.
    }

    @Override
    public Host find(final String domain) {
        return new HostMocker().init().mock();
    }

    @Override
    public Set<Domain> domains(final User user) {
        return Collections.emptySet();
    }
}
