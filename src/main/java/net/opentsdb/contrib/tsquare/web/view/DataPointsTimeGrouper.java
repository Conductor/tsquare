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
package net.opentsdb.contrib.tsquare.web.view;

import java.util.Iterator;

import net.opentsdb.core.DataPoint;
import net.opentsdb.core.DataPoints;

import com.google.common.collect.Range;

/**
 * @author James Royalty (jroyalty) <i>[Jul 26, 2013]</i>
 */
public class DataPointsTimeGrouper {
    public static final Double VALUE_WHEN_EXHAUSTED = Double.valueOf(Double.NaN);
    
    private DataPoints series;
    private DataPoint currentDataPoint;
    private Iterator<DataPoint> iterator;
    
    private boolean exhausted;
    
    public DataPointsTimeGrouper(final DataPoints series) {
        this.series = series;
        this.iterator = series.iterator();
        
        if (this.iterator.hasNext()) {
            this.currentDataPoint = this.iterator.next();
            this.exhausted = false;
        } else {
            this.currentDataPoint = null;
            this.exhausted = true;
        }
    }
    
    public Double advance() {
        if (exhausted) {
            return VALUE_WHEN_EXHAUSTED;
        } else if (iterator.hasNext()) {
            currentDataPoint = iterator.next();
            return getCurrentValue();
        } else {
            exhausted = true;
            return null;
        }
    }
    
    public Double getThenAdvanceIfWithinRange(final Range<Long> timestampRange) {
        if (exhausted) {
            return VALUE_WHEN_EXHAUSTED;
        } else if (timestampRange.contains(currentDataPoint.timestamp())) {
            final double value = currentDataPoint.toDouble();
            advance();
            return Double.valueOf(value);
        } else {
            return null;
        }
    }
    
    public Double getCurrentValue() {
        if (exhausted) {
            return VALUE_WHEN_EXHAUSTED;
        } else {
            return Double.valueOf(currentDataPoint.toDouble());
        }
    }
    
    public long getCurrentTimestamp() {
        if (exhausted) {
            return Long.MIN_VALUE;
        } else {
            return currentDataPoint.timestamp();
        }
    }
    
    public String getMetricName() {
        return series.metricName();
    }
    
    public boolean isExhausted() {
        return exhausted;
    }
}
