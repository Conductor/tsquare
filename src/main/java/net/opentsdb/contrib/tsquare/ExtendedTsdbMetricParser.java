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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.opentsdb.core.Aggregator;
import net.opentsdb.core.Tags;

import org.springframework.util.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Parser for an extended version of the Open TSDB v1.x metric format.
 * 
 * @author James Royalty (jroyalty) <i>[Jun 13, 2013]</i>
 */
public class ExtendedTsdbMetricParser implements MetricParser {
    public static final String AUTO_AGGREGATOR = "auto";
    
    private AggregatorFactory aggregatorFactory;
    private Aggregator defaultAggregator;
    
    public ExtendedTsdbMetricParser(final AggregatorFactory aggregatorFactory, final Aggregator defaultAggregator) {
        this.aggregatorFactory = aggregatorFactory;
        this.defaultAggregator = defaultAggregator;
    }
    
    @Override
    public List<Metric> parseMetrics(final Iterable<String> metrics) {
        List<Metric> parsedMetrics = Lists.newArrayList();
        
        for (final String metricString : metrics) {
            final Metric metric = parseMetric(metricString);
            parsedMetrics.add(metric);
        }
        
        return parsedMetrics;
    }
    
    @Override
    public Metric parseMetric(final String metricString) {
        final Metric metric;
        
        if (metricString.endsWith("}")) {
            HashMap<String, String> tags = Maps.newHashMap();
            final String metricSubstring = Tags.parseWithMetric(metricString, tags);
            
            metric = parseSingleMetricWithoutTags(metricSubstring);
            metric.setTags(tags);
        } else {
            metric = parseSingleMetricWithoutTags(metricString);
        }
        
        return metric;
    }
    
    private Metric parseSingleMetricWithoutTags(final String metricString) {
        Metric metric = null;
        
        if (metricString.indexOf(':') < 0) {
            // EXTENSION: This is a metric only expression. We auto-detect the aggregator
            // based on the metric name, or use the default.
            Aggregator agg = aggregatorFactory.getAggregatorForMetric(metricString);
            metric = new Metric(metricString, metricString, (agg == null ? defaultAggregator : agg));
        } else {
            // Otherwise, this might be a standard expression with some extra sauce...
            final String[] parts = StringUtils.delimitedListToStringArray(metricString, ":");
            Preconditions.checkState(parts.length > 1, "Invalid metric: %s", metricString);

            // Metric name is always the last element, regardless of format.  ASSUMING we've stripped
            // tag expressions off the end.
            final String metricName = parts[parts.length-1];
            
            // EXTENSION: Logic in determineAggregator() allows for empty or auto-detect 
            // aggregators.  See the doc on that method for details.
            metric = new Metric(metricString, metricName, determineAggregator(metricName, parts[0]));
            
            // Handle parsing of rate and downsampler.
            if (parts.length > 2 && parts.length <= 4) {
                for (int i=1; i<parts.length-1; i++) {
                    final String p = parts[i];
                    if ("rate".equals(p)) {
                        metric.setRate(true);
                    } else if (Character.isDefined(p.charAt(0)) && p.indexOf('-') > 0) { // 1h-sum
                        final String[] downsampleParts = StringUtils.split(p, "-");
                        long downsampleMillis = new DateTimeExpressionParser()
                            .setBaseTimeMillis(0) // ... parse a relative duration
                            .setPositiveOffset(true)
                            .parseRequired(downsampleParts[0]);
                        
                        // Convert to epoch SECONDS.
                        metric.setDownsampleIntervalSeconds((int) TimeUnit.MILLISECONDS.toSeconds(downsampleMillis));
                        
                        // EXTENSION: Again, determineAggregator() allows for empty or auto-detected
                        // downsamplers.
                        metric.setDownsampler(determineAggregator(metricName, downsampleParts[1]));
                    } else {
                        throw new IllegalStateException("Invalid characters in metric: " + p);
                    }
                }
            } else if (parts.length != 2) {
                throw new IllegalStateException("Metric has invalid parsed length: " + parts.length);
            }
        }
        
        return metric;
    }
    
    private Aggregator determineAggregator(final String metricName, final String aggregatorName) {
        Aggregator agg;
        
        if (StringUtils.isEmpty(aggregatorName) || AUTO_AGGREGATOR.equals(aggregatorName)) {
            agg = aggregatorFactory.getAggregatorForMetric(metricName);
            agg = (agg == null) ? defaultAggregator : agg;
        } else {
            agg = aggregatorFactory.getAggregatorByName(aggregatorName);
            Preconditions.checkState(agg != null, "Unknown aggregator: %s", aggregatorName);
        }
        
        return agg;
    }
}
