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
package net.opentsdb.contrib.tsquare.support;

import java.util.Iterator;

import net.opentsdb.core.Aggregator.Doubles;
import net.opentsdb.core.DataPoint;
import net.opentsdb.core.DataPoints;

/**
 * Wraps a {@link DataPoints} object and exposes the data consistent
 * with the {@link Doubles} interface.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 12, 2013]</i>
 */
public class DataPointsAsDoubles implements Doubles {
    private final Iterator<DataPoint> iterator;
    
    public DataPointsAsDoubles(final DataPoints points) {
        this.iterator = points.iterator();
    }
    
    @Override
    public boolean hasNextValue() {
        return iterator.hasNext();
    }

    @Override
    public double nextDoubleValue() {
        return iterator.next().toDouble();
    }
}
