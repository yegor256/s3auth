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
package com.s3auth.hosts;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.aspects.Tv;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Default implementation of {@link Bucket}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@Immutable
@EqualsAndHashCode(of = "domain")
@Loggable(Loggable.DEBUG)
final class DefaultBucket implements Bucket {

    /**
     * The domain.
     */
    private final transient Domain domain;

    /**
     * Package-private constructor.
     * @param dmn The domain
     */
    DefaultBucket(@NotNull final Domain dmn) {
        this.domain = dmn;
    }

    @Override
    @NotNull
    @Cacheable(lifetime = Tv.TEN, unit = TimeUnit.MINUTES)
    public AmazonS3 client() {
        final AmazonS3 client = new AmazonS3Client(
            new BasicAWSCredentials(this.domain.key(), this.domain.secret()),
            new ClientConfiguration()
                .withSocketTimeout(0)
                .withProtocol(Protocol.HTTP)
        );
        client.setEndpoint(
            String.format("%s.amazonaws.com", this.domain.region())
        );
        return client;
    }

    @Override
    public String toString() {
        return this.domain.toString();
    }

    @Override
    @NotNull
    public String name() {
        return this.domain.name();
    }

    @Override
    @NotNull
    public String key() {
        return this.domain.key();
    }

    @Override
    @NotNull
    public String secret() {
        return this.domain.secret();
    }

    @Override
    @NotNull
    public String bucket() {
        return this.domain.bucket();
    }

    @Override
    @NotNull
    public String region() {
        return this.domain.region();
    }

    @Override
    public String syslog() {
        return this.domain.syslog();
    }

}
