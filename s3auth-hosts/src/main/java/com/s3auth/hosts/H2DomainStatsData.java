/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2025, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.jdbc.JdbcSession;
import com.jcabi.jdbc.Outcome;
import com.jcabi.jdbc.UrlSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Storage of {@link Stats} per domain with H2 Database.
 *
 * @since 0.0.1
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
        .append("ID INT PRIMARY KEY auto_increment,")
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
    private static final Outcome<Stats> STATS = (rset, stmt) -> {
        rset.next();
        return new Stats.Simple(rset.getLong(1));
    };

    /**
     * Outcome for obtaining a single Stats for all domains.
     */
    private static final Outcome<Map<String, Stats>> STATS_ALL = (rset, stmt) -> {
        final Map<String, Stats> stats = new HashMap<>();
        while (rset.next()) {
            stats.put(
                rset.getString(1), new Stats.Simple(rset.getLong(2))
            );
        }
        return stats;
    };

    /**
     * The JDBC URL.
     */
    private final transient String jdbc;

    /**
     * Public ctor.
     */
    H2DomainStatsData() {
        this(new File("s3auth-domainStats"));
    }

    /**
     * Public ctor.
     * @param file The file pointing to the database to use.
     */
    H2DomainStatsData(final File file) {
        this.jdbc = String.format("jdbc:h2:file:%s", file.getAbsolutePath());
    }

    /**
     * Create tables.
     * @return This
     * @throws IOException If an IO Exception occurs.
     */
    public H2DomainStatsData init() throws IOException {
        try {
            this.session().sql(H2DomainStatsData.CREATE).execute().commit();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
        return this;
    }

    @Override
    public void put(final String domain, final Stats stats) throws IOException {
        try {
            this.session()
                .sql(H2DomainStatsData.INSERT)
                .set(domain)
                .set(stats.bytesTransferred())
                .execute()
                .commit();
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public Stats get(final String domain) throws IOException {
        try {
            final JdbcSession session = this.session();
            // @checkstyle LineLength (2 lines)
            final Stats result = session
                .sql("SELECT SUM(BYTES) FROM DOMAIN_STATS WHERE DOMAIN = ?")
                .set(domain)
                .select(H2DomainStatsData.STATS);
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
            final JdbcSession session = this.session();
            // @checkstyle LineLength (2 lines)
            final Map<String, Stats> result = session
                .sql("SELECT DOMAIN, SUM(BYTES) FROM DOMAIN_STATS GROUP BY DOMAIN")
                .select(H2DomainStatsData.STATS_ALL);
            session.sql("DELETE FROM DOMAIN_STATS").execute().commit();
            return result;
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }

    /**
     * Make new session.
     * @return Session
     */
    private JdbcSession session() {
        return new JdbcSession(
            new UrlSource(this.jdbc)
        ).autocommit(false);
    }

}
