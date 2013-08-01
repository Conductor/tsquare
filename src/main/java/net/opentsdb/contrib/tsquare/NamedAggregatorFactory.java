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

/**
 * Factory for {@link Aggregator}s, using the aggregator's name as a 
 * lookup method.
 * 
 * @author James Royalty (jroyalty) <i>[Aug 1, 2013]</i>
 */
public interface NamedAggregatorFactory {
    /**
     * @param aggregatorName
     * @return <code>null</code> if no aggregator is registered under
     * the given name.
     */
    Aggregator getAggregatorByName(String aggregatorName);
}
