/**
 * Copyright (c) 2012-2014, s3auth.com
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

import com.jcabi.aspects.Loggable;
import com.rexsl.page.Link;
import com.s3auth.hosts.Domain;
import com.s3auth.hosts.Stats;
import java.util.Collection;
import java.util.LinkedList;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JAXB domain.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
@XmlRootElement(name = "domain")
@XmlAccessorType(XmlAccessType.NONE)
@Loggable(Loggable.DEBUG)
public final class JaxbDomain {

    /**
     * The domain.
     */
    private final transient Domain domain;

    /**
     * The URI info.
     */
    private final transient UriInfo info;

    /**
     * The stats for this domain.
     */
    private final transient JaxbStats stats;

    /**
     * Public ctor for JAXB.
     */
    public JaxbDomain() {
        throw new IllegalStateException("This ctor should never be called");
    }

    /**
     * Private ctor.
     * @param dmn The domain
     * @param inf URI info of the home
     * @param statistics The stats for this domain
     */
    public JaxbDomain(final Domain dmn, final UriInfo inf,
        final Stats statistics) {
        this.domain = dmn;
        this.info = inf;
        this.stats = new JaxbStats(statistics);
    }

    /**
     * Get name.
     * @return The name
     */
    @XmlElement
    public String getName() {
        return this.domain.name();
    }

    /**
     * Get key.
     * @return The key
     */
    @XmlElement
    public String getKey() {
        return this.domain.key();
    }

    /**
     * Get secret.
     * @return The secret
     */
    @XmlElement
    public String getSecret() {
        return this.domain.secret();
    }

    /**
     * Get bucket.
     * @return The bucket
     */
    @XmlElement
    public String getBucket() {
        return this.domain.bucket();
    }

    /**
     * Get region.
     * @return The region
     */
    @XmlElement
    public String getRegion() {
        return this.domain.region();
    }

    /**
     * Get syslog.
     * @return The syslog
     */
    @XmlElement
    public String getSyslog() {
        return this.domain.syslog();
    }

    /**
     * Get stats for this domain.
     * @return The stats
     */
    @XmlElement
    public JaxbStats getStats() {
        return this.stats;
    }

    /**
     * Get links.
     * @return The links
     */
    @XmlElement(name = "link")
    @XmlElementWrapper(name = "links")
    public Collection<Link> getLinks() {
        final Collection<Link> links = new LinkedList<Link>();
        links.add(
            new Link(
                "remove",
                this.info.getBaseUriBuilder().clone()
                    .path("/remove")
                    .queryParam("host", "{name}")
                    .build(this.getName())
            )
        );
        return links;
    }

    /**
     * JAXB domain stats.
     */
    @XmlRootElement(name = "stats")
    private static final class JaxbStats {
        /**
         * The underlying stats.
         */
        private final transient Stats stats;
        /**
         * Default ctor required by JAXB.
         */
        @SuppressWarnings("unused")
        JaxbStats() {
            throw new IllegalStateException(
                "Default constructor should never be called"
            );
        }
        /**
         * Constructor.
         * @param statistics The stats for this domain.
         */
        JaxbStats(final Stats statistics) {
            this.stats = statistics;
        }
        /**
         * Get bytes transferred.
         * @return Bytes transferred.
         */
        @XmlElement
        public long getBytesTransferred() {
            return this.stats.bytesTransferred();
        }
    }

}
