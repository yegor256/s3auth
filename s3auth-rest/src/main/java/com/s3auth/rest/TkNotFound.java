/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import java.io.IOException;
import java.net.HttpURLConnection;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;
import org.takes.rs.RsWithStatus;

/**
 * Not found page.
 *
 * @since 0.1
 */
final class TkNotFound implements Take {

    @Override
    public Response act(final Request request) throws IOException {
        return new RsWithStatus(
            new RsPage(
                "/xsl/404.xsl",
                request
            ),
            HttpURLConnection.HTTP_NOT_FOUND
        );
    }

}
