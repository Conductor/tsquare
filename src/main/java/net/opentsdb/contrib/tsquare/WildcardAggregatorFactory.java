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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PatternMatchUtils;

/**
 * @author James Royalty (jroyalty) <i>[Jun 4, 2013]</i>
 */
public class WildcardAggregatorFactory extends AbstractAggregatorFactory {
    private static final Logger log = LoggerFactory.getLogger(WildcardAggregatorFactory.class);
    
    private Map<String, Aggregator> aggregatorsByWildcard = Collections.emptyMap();
    
    @Override
    public Aggregator getAggregatorForMetric(String metricName) {
        for (final Map.Entry<String, Aggregator> entry : aggregatorsByWildcard.entrySet()) {
            if (PatternMatchUtils.simpleMatch(entry.getKey(), metricName)) {
                log.debug("Metric {} matched wildcard {} -> {}", metricName, entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }
        
        return null;
    }

    public void setAggregatorsByWildcard(Map<String, Aggregator> aggregatorsByWildcard) {
        this.aggregatorsByWildcard = aggregatorsByWildcard;
    }
}
