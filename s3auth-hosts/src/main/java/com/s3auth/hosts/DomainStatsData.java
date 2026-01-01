/*
 * SPDX-FileCopyrightText: Copyright (c) 2012-2026, Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.s3auth.hosts;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.util.Map;

/**
 * Store of {@link Stats} per domain.
 * @since 0.0.1
 */
@Immutable
interface DomainStatsData {

    /**
     * Post the statistics of the given domain, for this particular time.
     * @param domain The domain of this stats.
     * @param stats The stats to keep.
     * @throws IOException If something goes wrong.
     */
    void put(String domain, Stats stats) throws IOException;

    /**
     * Get the stats for the given domain.
     * @param domain The domain whose stats we're interested in
     * @return The stats for this domain
     * @throws IOException If something goes wrong.
     */
    Stats get(String domain) throws IOException;

    /**
     * Get the stats for all domains.
     * @return Map of each domain and their corresponding stats.
     * @throws IOException If something goes wrong.
     */
    Map<String, Stats> all() throws IOException;

}
