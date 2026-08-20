/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

/**
 * Outcome of one attempt to fetch an object by name.
 * @since 0.0.1
 */
final class Attempt {

    /**
     * Resource found, if any.
     */
    private final transient Resource resource;

    /**
     * Whether no other name should be tried.
     */
    private final transient boolean done;

    /**
     * Ctor.
     * @param found Resource found, if any
     * @param fin Whether no other name should be tried
     */
    Attempt(final Resource found, final boolean fin) {
        this.resource = found;
        this.done = fin;
    }

    /**
     * Resource found, if any.
     * @return The resource, or null
     */
    Resource resource() {
        return this.resource;
    }

    /**
     * Whether no other name should be tried.
     * @return TRUE if done
     */
    boolean done() {
        return this.done;
    }
}
