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

import net.opentsdb.core.Aggregator;
import net.opentsdb.core.Aggregators.Interpolation;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.base.Preconditions;

/**
 * Aggregator that computes a percentile using a limited window size.
 * 
 * @author James Royalty (jroyalty) <i>[Aug 1, 2013]</i>
 */
public class PercentileAggregator implements Aggregator {
    private final double percentile;
    private final int windowSize;
    
    public PercentileAggregator() {
        percentile = 90.0d;
        windowSize = 1000;
    }
    
    public PercentileAggregator(final double percentile) {
        Preconditions.checkArgument(percentile > 0 && percentile <= 100.d, "Invalid percentile value");
        
        this.percentile = percentile;
        windowSize = 1000;
    }
    
    public PercentileAggregator(final double percentile, final int windowSize) {
        Preconditions.checkArgument(percentile > 0 && percentile <= 100.d, "Invalid percentile value");
        
        this.percentile = percentile;
        this.windowSize = windowSize;
    }
    
    @Override
    public long runLong(final Longs values) {
        DescriptiveStatistics stats = new DescriptiveStatistics(windowSize);
        
        while (values.hasNextValue()) {
            stats.addValue(values.nextLongValue());
        }
        
        return (long) stats.getPercentile(percentile);
    }

    @Override
    public double runDouble(final Doubles values) {
        DescriptiveStatistics stats = new DescriptiveStatistics(windowSize);
        
        while (values.hasNextValue()) {
            stats.addValue(values.nextDoubleValue());
        }
        
        return stats.getPercentile(percentile);
    }

    @Override
    public Interpolation interpolationMethod() {
        return Interpolation.LERP;
    }
}
