/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.rest;

import java.io.IOException;
import org.takes.Request;
import org.takes.Response;
import org.takes.Take;

/**
 * Index page of an anonymous user.
 *
 * @since 0.1
 */
final class TkLogin implements Take {

    @Override
    public Response act(final Request request) throws IOException {
        return new RsPage(
            "/xsl/login.xsl",
            request
        );
    }

}
