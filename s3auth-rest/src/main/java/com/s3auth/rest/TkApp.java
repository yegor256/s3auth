/**
 * Copyright (c) 2012-2015, s3auth.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the s3auth.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.s3auth.rest;

import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.Hosts;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.takes.Response;
import org.takes.Take;
import org.takes.facets.auth.PsByFlag;
import org.takes.facets.auth.PsChain;
import org.takes.facets.auth.PsCookie;
import org.takes.facets.auth.PsFake;
import org.takes.facets.auth.PsLogout;
import org.takes.facets.auth.TkAuth;
import org.takes.facets.auth.codecs.CcCompact;
import org.takes.facets.auth.codecs.CcHex;
import org.takes.facets.auth.codecs.CcSafe;
import org.takes.facets.auth.codecs.CcSalted;
import org.takes.facets.auth.codecs.CcXOR;
import org.takes.facets.auth.social.PsFacebook;
import org.takes.facets.auth.social.PsGithub;
import org.takes.facets.auth.social.PsGoogle;
import org.takes.facets.fallback.Fallback;
import org.takes.facets.fallback.FbChain;
import org.takes.facets.fallback.FbStatus;
import org.takes.facets.fallback.RqFallback;
import org.takes.facets.fallback.TkFallback;
import org.takes.facets.flash.TkFlash;
import org.takes.facets.fork.FkAnonymous;
import org.takes.facets.fork.FkAuthenticated;
import org.takes.facets.fork.FkParams;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.facets.forward.TkForward;
import org.takes.rs.RsVelocity;
import org.takes.rs.RsWithStatus;
import org.takes.rs.RsWithType;
import org.takes.tk.TkClasspath;
import org.takes.tk.TkGzip;
import org.takes.tk.TkMeasured;
import org.takes.tk.TkRedirect;
import org.takes.tk.TkText;
import org.takes.tk.TkVersioned;
import org.takes.tk.TkWithHeaders;
import org.takes.tk.TkWithType;
import org.takes.tk.TkWrap;

/**
 * Takes app.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.1
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 * @checkstyle ClassFanOutComplexityCheck (500 lines)
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class TkApp extends TkWrap {

    /**
     * Revision of the package.
     */
    private static final String REV = Manifests.read("S3Auth-Revision");

    /**
     * Ctor.
     * @param hosts Hosts
     */
    public TkApp(@NotNull final Hosts hosts) {
        super(TkApp.make(hosts));
    }

    /**
     * Make it.
     * @param hosts Hosts
     * @return Take
     */
    private static Take make(final Hosts hosts) {
        if (!"UTF-8".equals(Charset.defaultCharset().name())) {
            throw new IllegalStateException(
                String.format(
                    "default encoding is %s", Charset.defaultCharset()
                )
            );
        }
        final Take take = new TkGzip(
            TkApp.fallback(
                new TkFlash(
                    TkApp.auth(
                        new TkForward(TkApp.regex(hosts))
                    )
                )
            )
        );
        return new TkWithHeaders(
            new TkVersioned(new TkMeasured(take)),
            String.format("X-S3Auth-Revision: %s", TkApp.REV),
            "Vary: Cookie"
        );
    }

    /**
     * Authenticated.
     * @param takes Take
     * @return Authenticated takes
     */
    private static Take fallback(final Take takes) {
        return new TkFallback(
            takes,
            new FbChain(
                new FbStatus(
                    HttpURLConnection.HTTP_NOT_FOUND,
                    new TkNotFound()
                ),
                // @checkstyle AnonInnerLengthCheck (50 lines)
                new Fallback() {
                    @Override
                    public Iterator<Response> route(final RqFallback req)
                        throws IOException {
                        final String err = ExceptionUtils.getStackTrace(
                            req.throwable()
                        );
                        return Collections.<Response>singleton(
                            new RsWithStatus(
                                new RsWithType(
                                    new RsVelocity(
                                        this.getClass().getResource(
                                            "exception.html.vm"
                                        ),
                                        new RsVelocity.Pair("err", err),
                                        new RsVelocity.Pair("rev", TkApp.REV)
                                    ),
                                    "text/html"
                                ),
                                HttpURLConnection.HTTP_INTERNAL_ERROR
                            )
                        ).iterator();
                    }
                }
            )
        );
    }

    /**
     * Authenticated.
     * @param takes Take
     * @return Authenticated takes
     */
    private static Take auth(final Take takes) {
        return new TkAuth(
            takes,
            new PsChain(
                new PsFake(
                    Manifests.read("S3Auth-AwsDynamoKey").startsWith("AAAA")
                ),
                new PsByFlag(
                    new PsByFlag.Pair(
                        PsGithub.class.getSimpleName(),
                        new PsGithub(
                            Manifests.read("S3Auth-GithubId"),
                            Manifests.read("S3Auth-GithubSecret")
                        )
                    ),
                    new PsByFlag.Pair(
                        PsFacebook.class.getSimpleName(),
                        new PsFacebook(
                            Manifests.read("S3Auth-FbId"),
                            Manifests.read("S3Auth-FbSecret")
                        )
                    ),
                    new PsByFlag.Pair(
                        PsGoogle.class.getSimpleName(),
                        new PsGoogle(
                            Manifests.read("S3Auth-GoogleId"),
                            Manifests.read("S3Auth-GoogleSecret"),
                            "http://www.s3auth.com/?PsByFlag=PsGoogle"
                        )
                    ),
                    new PsByFlag.Pair(
                        PsLogout.class.getSimpleName(),
                        new PsLogout()
                    )
                ),
                new PsCookie(
                    new CcSafe(
                        new CcHex(
                            new CcXOR(
                                new CcSalted(new CcCompact()),
                                Manifests.read("S3Auth-SecurityKey")
                            )
                        )
                    )
                )
            )
        );
    }

    /**
     * Regex takes.
     * @param hosts Hosts
     * @return Take
     */
    private static Take regex(final Hosts hosts) {
        return new TkFork(
            new FkParams(
                PsByFlag.class.getSimpleName(),
                Pattern.compile(".+"),
                new TkRedirect()
            ),
            new FkRegex("/robots.txt", ""),
            new FkRegex(
                "/xsl/[a-z0-9]+\\.xsl",
                new TkWithType(new TkClasspath(), "text/xsl")
            ),
            new FkRegex(
                "/css/[a-z]+\\.css",
                new TkWithType(new TkClasspath(), "text/css")
            ),
            new FkRegex(
                "/",
                new TkFork(
                    new FkAuthenticated(new TkIndex(hosts)),
                    new FkAnonymous(new TkLogin())
                )
            ),
            new FkRegex("/add", new TkAdd(hosts)),
            new FkRegex("/remove", new TkRemove(hosts)),
            new FkRegex("/version", new TkText(TkApp.REV)),
            new FkRegex(
                "/license",
                new TkText(TkApp.class.getResourceAsStream("/LICENSE.txt"))
            )
        );
    }
}
