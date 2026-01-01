/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.mockito.Mockito;

/**
 * Mocker of {@link HttpRequest}.
 *
 * @since 0.0.1
 */
public final class HttpRequestMocker {

    /**
     * It's a utility class at the moment.
     */
    private HttpRequestMocker() {
        // intentionally empty
    }

    /**
     * Convert string to request.
     * @param text The text
     * @return Requests
     * @throws Exception If there is some problem inside
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static HttpRequest toRequest(final String text) throws Exception {
        final Socket socket = Mockito.mock(Socket.class);
        Mockito.doReturn(IOUtils.toInputStream(text, StandardCharsets.UTF_8))
            .when(socket).getInputStream();
        return new HttpRequest(socket);
    }

}
