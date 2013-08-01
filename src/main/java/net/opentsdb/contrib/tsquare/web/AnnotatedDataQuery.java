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

import net.opentsdb.contrib.tsquare.Metric;
import net.opentsdb.core.Query;

import com.google.common.base.Objects;

/**
 * Combines a {@link Metric} and a {@link Query}.  The query is assumed to 
 * be ready to execute.  This is used to send model data to the view for
 * stream-based rendering.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 25, 2013]</i>
 */
public class AnnotatedDataQuery {
    private Metric metric;
    private Query query;
    
    public AnnotatedDataQuery(final Metric metric, final Query query) {
        this.metric = metric;
        this.query = query;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(metric.getRawMetric(), query.getStartTime(), query.getEndTime());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AnnotatedDataQuery)) {
            return false;
        }
        
        final AnnotatedDataQuery other = (AnnotatedDataQuery) obj;
        return metric.getRawMetric().equals(other.metric.getRawMetric())
                && query.getStartTime() == other.query.getStartTime()
                && query.getEndTime() == other.query.getEndTime();
    }

    public Metric getMetric() {
        return metric;
    }

    public Query getQuery() {
        return query;
    }
}
