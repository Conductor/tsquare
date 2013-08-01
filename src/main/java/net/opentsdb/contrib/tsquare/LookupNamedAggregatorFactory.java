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

import java.util.Map;

import net.opentsdb.core.Aggregator;

import com.google.common.collect.Maps;

/**
 * @author James Royalty (jroyalty) <i>[Aug 1, 2013]</i>
 */
public class LookupNamedAggregatorFactory implements NamedAggregatorFactory {
    private Map<String, Aggregator> aggregators;
    
    @Override
    public Aggregator getAggregatorByName(String aggregatorName) {
        return aggregators.get(aggregatorName.toLowerCase());
    }

    public void setAggregators(Map<String, Aggregator> aggregatorsMap) {
        this.aggregators = Maps.newLinkedHashMap();
        
        for (final Map.Entry<String, Aggregator> entry : aggregatorsMap.entrySet()) {
            this.aggregators.put(entry.getKey().toLowerCase(), entry.getValue());
        }
    }
}
