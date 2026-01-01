/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.relay;

import java.io.ByteArrayOutputStream;
import java.net.Socket;
import org.mockito.Mockito;

/**
 * Mocker of {@link HttpResponse}.
 *
 * @since 0.0.1
 */
public final class HttpResponseMocker {

    /**
     * It's a utility class at the moment.
     */
    private HttpResponseMocker() {
        // intentionally empty
    }

    /**
     * Convert response to string.
     * @param resp The response
     * @return Text form
     * @throws Exception If there is some problem inside
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static String toString(final HttpResponse resp) throws Exception {
        final Socket socket = Mockito.mock(Socket.class);
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Mockito.doReturn(stream).when(socket).getOutputStream();
        resp.send(socket);
        return new String(stream.toByteArray());
    }

}
