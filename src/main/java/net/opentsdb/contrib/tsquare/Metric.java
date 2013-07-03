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
package net.opentsdb.contrib.tsquare;

import java.util.Collections;
import java.util.Map;

import net.opentsdb.core.Aggregator;
import net.opentsdb.core.Query;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * @author James Royalty (jroyalty) <i>[Jun 13, 2013]</i>
 */
public class Metric {
    private String rawMetric;
    private String name;
    private Map<String, String> tags;
    private int downsampleIntervalSeconds;
    private Aggregator aggregator;
    private Aggregator downsampler;
    private boolean rate;
    
    public Metric(final String rawMetric, final String name, final Aggregator aggregator) {
        this.name = name;
        this.aggregator = aggregator;
        this.rate = false;
    }
    
    public Query contributeToQuery(final Query query) {
        query.setTimeSeries(
                name,
                (tags == null ? Collections. <String, String> emptyMap() : tags),
                aggregator,
                rate);
        
        if (downsampler != null && downsampleIntervalSeconds > 0) {
            query.downsample(downsampleIntervalSeconds, downsampler);
        }
        
        return query;
    }
    
    @Override
    public String toString() {
        final ToStringHelper helper = Objects.toStringHelper(this);
        helper.add("name", name).add("agg", aggregator);
        if (tags != null) {
            helper.add("tags", tags);
        }
        return helper.toString();
    }
    
    public String getRawMetric() {
        return rawMetric;
    }

    public String getName() {
        return name;
    }

    public Aggregator getAggregator() {
        return aggregator;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Metric setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    public int getDownsampleIntervalSeconds() {
        return downsampleIntervalSeconds;
    }

    public Metric setDownsampleIntervalSeconds(int downsampleIntervalSeconds) {
        this.downsampleIntervalSeconds = downsampleIntervalSeconds;
        return this;
    }

    public Aggregator getDownsampler() {
        return downsampler;
    }

    public Metric setDownsampler(Aggregator downsampler) {
        this.downsampler = downsampler;
        return this;
    }

    public boolean isRate() {
        return rate;
    }

    public Metric setRate(boolean rate) {
        this.rate = rate;
        return this;
    }
}
