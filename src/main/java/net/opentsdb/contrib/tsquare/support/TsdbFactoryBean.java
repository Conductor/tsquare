/*
 * Copyright (C) 2015 Conductor, Inc.
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

import com.google.common.base.Preconditions;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import net.opentsdb.core.TSDB;
import net.opentsdb.utils.Config;

/**
 * Create a {@link TSDB} instance using the {@link Config} instance loaded 
 * by {@TsWebApplicationContextInitializer}.
 * 
 * @author James Royalty (jroyalty) <i>[Jan 13, 2015]</i>
 */
public class TsdbFactoryBean implements FactoryBean<TSDB> {
    @Autowired
    private ConfigurableEnvironment springEnvironment;
    
    private TSDB instance;
    
    @Override
    public TSDB getObject() throws Exception {
        if (instance == null) {
            @SuppressWarnings("unchecked")
            PropertySource<Config> tsdbPropertySource = (PropertySource<Config>) springEnvironment.getPropertySources().get(TsWebApplicationContextInitializer.PROPERTY_SOURCE_NAME);
            Preconditions.checkNotNull(tsdbPropertySource, "Unable to load TSDB property source");
            instance = new TSDB(tsdbPropertySource.getSource());
        }
        
        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return TSDB.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
