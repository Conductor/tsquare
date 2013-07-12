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
package net.opentsdb.contrib.tsquare.controller;

import net.opentsdb.contrib.tsquare.Metric;
import net.opentsdb.core.Query;

/**
 * Holder of query data and the parsed {@link Metric} it's for.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 12, 2013]</i>
 */
final class QueryMetaData {
    private Metric metric;
    private Query query;
    
    public QueryMetaData(final Metric metric, final Query query) {
        this.metric = metric;
        this.query = query;
    }

    public Metric getMetric() {
        return metric;
    }

    public Query getQuery() {
        return query;
    }
}
