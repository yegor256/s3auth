/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Mocker of HTTP port.
 *
 * @since 0.0.1
 */
public final class PortMocker {

    /**
     * It's a utility class at the moment.
     */
    private PortMocker() {
        // intentionally empty
    }

    /**
     * Find and return the first available port.
     *
     * @return The port number
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static int reserve() {
        final int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to reserve port", ex);
        }
        return port;
    }
}
