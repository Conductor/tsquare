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
import net.opentsdb.contrib.tsquare.web.view.DataQueryResponseWriter;
import net.opentsdb.core.DataPoints;

import com.google.common.collect.Range;

/**
 * Combines {@link Metric} and a given {@link DataPoints}.  This is used to
 * send query results to implementations of {@link DataQueryResponseWriter}.
 * Keep in mind the semantics of {@link DataPoints} when holding a reference 
 * to this object.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 29, 2013]</i>
 */
public class AnnotatedDataPoints {
    private Metric metric;
    private DataPoints dataPoints;
    private Range<Long> queryRangeInSeconds;
    
    public AnnotatedDataPoints(final Metric metric, final Range<Long> queryRangeInSeconds, final DataPoints dataPoints) {
        this.metric = metric;
        this.queryRangeInSeconds = queryRangeInSeconds;
        this.dataPoints = dataPoints;
    }

    public Metric getMetric() {
        return metric;
    }

    public DataPoints getDataPoints() {
        return dataPoints;
    }

    public Range<Long> getQueryRangeInSeconds() {
        return queryRangeInSeconds;
    }
}