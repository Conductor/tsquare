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

/**
 * @author James Royalty (jroyalty) <i>[Jun 26, 2013]</i>
 */
public final class LastValueAggregator implements Aggregator {
    @Override
    public long runLong(final Longs values) {
        long last = 0;
        
        while (values.hasNextValue()) {
            last = values.nextLongValue();
        }
        
        return last;
    }

    @Override
    public double runDouble(final Doubles values) {
        double last = 0d;
        
        while (values.hasNextValue()) {
            last = values.nextDoubleValue();
        }
        
        return last;
    }

    @Override
    public Interpolation interpolationMethod() {
        return Interpolation.ZIM;
    }
}
