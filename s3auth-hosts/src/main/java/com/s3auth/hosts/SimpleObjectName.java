/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import java.util.Objects;

/**
 * Object name.
 * @since 0.0.1
 */
final class SimpleObjectName implements DefaultHost.ObjectName {

    /**
     * Original name.
     */
    private final transient String name;

    /**
     * Public ctor.
     * @param nme The name
     */
    SimpleObjectName(final String nme) {
        this.name = nme;
    }

    @Override
    public String get() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.name);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof SimpleObjectName
            && Objects.equals(this.name, ((SimpleObjectName) obj).name);
    }
}
