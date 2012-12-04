/**
 * Copyright (c) 2012, s3auth.com
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
package com.s3auth.servlets;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.jcabi.manifests.Manifests;
import com.s3auth.hosts.DynamoHosts;
import com.s3auth.hosts.Hosts;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Application-wide listener that initializes the application on start
 * and shuts it down on stop.
 *
 * <p>The class is mutable and NOT thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.0.1
 */
public final class HostsListener implements ServletContextListener {

    /**
     * When was it started.
     */
    private final transient long started = System.currentTimeMillis();

    /**
     * The hosts.
     */
    private transient Hosts hosts;

    /**
     * {@inheritDoc}
     *
     * <p>These attributes is used later in
     * {@link com.netbout.rest.BaseRs#setServletContext(ServletContext)}.
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void contextInitialized(final ServletContextEvent event) {
        try {
            Manifests.append(event.getServletContext());
            this.hosts = new DynamoHosts();
        } catch (java.io.IOException ex) {
            Logger.error(
                this,
                "#contextInitialized(): %[exception]s",
                ex
            );
            throw new IllegalStateException(ex);
        }
        event.getServletContext()
            .setAttribute("com.s3auth.HOSTS", this.hosts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Loggable(Loggable.DEBUG)
    public void contextDestroyed(final ServletContextEvent event) {
        if (this.hosts == null) {
            Logger.warn(this, "#contextDestroyed(): Hosts is null");
        } else {
            try {
                this.hosts.close();
            } catch (java.io.IOException ex) {
                Logger.error(
                    this,
                    "#contextDestroyed(): %[exception]s",
                    ex
                );
            }
        }
        Logger.info(
            this,
            "#contextDestroyed(): app was alive for %[ms]s",
            System.currentTimeMillis() - this.started
        );
        org.apache.log4j.LogManager.shutdown();
    }

}
