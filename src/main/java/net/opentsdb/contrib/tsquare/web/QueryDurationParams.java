/*
 * Copyright (C) 2013 Conductor, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.opentsdb.contrib.tsquare.web;

import com.google.common.base.Objects;

import net.opentsdb.core.Query;

import java.util.Date;

/**
 * @author James Royalty (jroyalty) <i>[Jun 26, 2013]</i>
 */
public final class QueryDurationParams {
    private final long nowMillis;
    private final long fromMillis;
    private final long untilMillis;
    
    public QueryDurationParams(final long nowMillis, final long fromMillis, final long untilMillis) {
        this.nowMillis = nowMillis;
        this.fromMillis = fromMillis;
        this.untilMillis = untilMillis;
    }
    
    public Query contributeToQuery(final Query query) {
        query.setStartTime(fromMillis);
        query.setEndTime(untilMillis);
        return query;
    }
    
    public long getNowMillis() {
        return nowMillis;
    }

    public long getFromMillis() {
        return fromMillis;
    }

    public long getUntilMillis() {
        return untilMillis;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("now", new Date(nowMillis))
                .add("from", new Date(fromMillis))
                .add("until", new Date(untilMillis))
                .toString();
    }
}
