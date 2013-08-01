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
import java.util.Set;

import net.opentsdb.core.Aggregator;

/**
 * Allows for {@link AggregatorFactory}s by-name and by-metric to be composed and provide
 * a single lookup strategy.  
 * 
 * @author James Royalty (jroyalty) <i>[Jun 17, 2013]</i>
 */
public class CompositeAggregatorFactory implements AggregatorFactory {
    private Set<NamedAggregatorFactory> factoriesForAggregatorName = Collections.emptySet();
    private Set<MetricAggregatorFactory> factoriesForMetricName = Collections.emptySet();
    
    @Override
    public Aggregator getAggregatorForMetric(final String metricName) {
        for (final MetricAggregatorFactory factory : factoriesForMetricName) {
            final Aggregator agg = factory.getAggregatorForMetric(metricName);
            if (agg != null) {
                return agg;
            }
        }
        
        return null;
    }
    
    @Override
    public Aggregator getAggregatorByName(final String aggregatorName) {
        for (final NamedAggregatorFactory factory : factoriesForAggregatorName) {
            final Aggregator agg = factory.getAggregatorByName(aggregatorName);
            if (agg != null) {
                return agg;
            }
        }
        
        return null;
    }

    public void setFactoriesForAggregatorName(Set<NamedAggregatorFactory> factoriesForAggregatorName) {
        this.factoriesForAggregatorName = factoriesForAggregatorName;
    }

    public void setFactoriesForMetricName(Set<MetricAggregatorFactory> factoriesForMetric) {
        this.factoriesForMetricName = factoriesForMetric;
    }
}
