/**
 * Copyright (c) 2012-2019, s3auth.com
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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.EqualsAndHashCode;
import org.h2.Driver;

/**
 * Storage of {@link Stats} per domain with H2 Database.
 *
 * @author Carlos Miranda (miranda.cma@gmail.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCouplingCheck (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "jdbc")
@Loggable(Loggable.DEBUG)
final class H2DomainStatsData implements DomainStatsData {
    /**
     * Create Table statement.
     */
    private static final String CREATE = new StringBuilder("CREATE TABLE ")
        .append("IF NOT EXISTS DOMAIN_STATS( ")
        .append("ID INT IDENTITY,")
        .append("DOMAIN VARCHAR(255),")
        .append("BYTES INT,")
        .append("CREATE_TIME TIMESTAMP")
        .append(" )").toString();

    /**
     * Insert statement.
     */
    private static final String INSERT = new StringBuilder("INSERT INTO ")
        .append("DOMAIN_STATS (DOMAIN, BYTES, CREATE_TIME) ")
        .append("values (?, ?, CURRENT_TIMESTAMP())").toString();

    /**
     * Outcome for obtaining a single Stats per domain.
     */
    private static final Outcome<Stats> STATS = new Outcome<Stats>() {
        @Override
        public Stats handle(final ResultSet rset, final Statement stmt)
            throws SQLException {
            rset.next();
            return new Stats.Simple(rset.getLong(1));
        }
    };

    /**
     * Outcome for obtaining a single Stats for all domains.
     */
    private static final Outcome<Map<String, Stats>> STATS_ALL =
        new Outcome<Map<String, Stats>>() {
            @Override
            @SuppressWarnings("PMD.UseConcurrentHashMap")
            public Map<String, Stats> handle(final ResultSet rset,
                final Statement stmt) throws SQLException {
                final Map<String, Stats> stats = new HashMap<String, Stats>();
                while (rset.next()) {
                    stats.put(
                        rset.getString(1), new Stats.Simple(rset.getLong(2))
                    );
                }
                return stats;
            }
        };

    /**
     * The JDBC URL.
     */
    private final transient String jdbc;

    /**
     * Public ctor.
     * @throws IOException If an IO Exception occurs.
     */
    H2DomainStatsData() throws IOException {
        this(new File("s3auth-domainStats"));
    }

    /**
     * Public ctor.
     * @param file The file pointing to the database to use.
     * @throws IOException If an IO Exception occurs.
     */
    H2DomainStatsData(final File file) throws IOException {
        this.jdbc = String.format("jdbc:h2:file:%s", file.getAbsolutePath());
        try {
            new JdbcSession(this.connection()).sql(CREATE).execute();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void put(final String domain, final Stats stats) throws IOException {
        try {
            new JdbcSession(this.connection())
                .sql(INSERT)
                .set(domain)
                .set(stats.bytesTransferred())
                .execute();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Stats get(final String domain) throws IOException {
        try {
            final JdbcSession session = new JdbcSession(this.connection())
                .autocommit(false);
            // @checkstyle LineLength (2 lines)
            final Stats result = session
                .sql("SELECT SUM(BYTES) FROM DOMAIN_STATS WHERE DOMAIN = ? FOR UPDATE")
                .set(domain)
                .select(STATS);
            session.sql("DELETE FROM DOMAIN_STATS WHERE DOMAIN = ?")
                .set(domain)
                .execute()
                .commit();
            return result;
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    public Map<String, Stats> all() throws IOException {
        try {
            final JdbcSession session = new JdbcSession(this.connection())
                .autocommit(false);
            // @checkstyle LineLength (2 lines)
            final Map<String, Stats> result = session
                .sql("SELECT DOMAIN, SUM(BYTES) FROM DOMAIN_STATS GROUP BY DOMAIN FOR UPDATE")
                .select(STATS_ALL);
            session.sql("DELETE FROM DOMAIN_STATS").execute().commit();
            return result;
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Make data source.
     * @return Data source for JDBC
     * @throws SQLException If it fails
     */
    private Connection connection() throws SQLException {
        return new Driver().connect(this.jdbc, new Properties());
    }
}
