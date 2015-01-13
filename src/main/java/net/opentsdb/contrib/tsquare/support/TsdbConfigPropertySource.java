/*
 * Copyright (C) 2014 Conductor, Inc.
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

import org.springframework.core.env.PropertySource;

import net.opentsdb.utils.Config;

/**
 * Uses TSDB's {@link Config} as a Spring property source.
 * 
 * @author James Royalty (jroyalty) <i>[Dec 19, 2014]</i>
 */
public class TsdbConfigPropertySource extends PropertySource<Config> {
    public TsdbConfigPropertySource(final String name, final Config tsdbConfig) {
        super(name, tsdbConfig);
    }

    @Override
    public Object getProperty(final String name) {
        return getSource().getMap().get(name);
    }
}
