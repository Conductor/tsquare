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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import net.opentsdb.utils.Config;

import java.io.IOException;

/**
 * @author James Royalty (jroyalty) <i>[Jun 17, 2013]</i>
 */
public class TsWebApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(TsWebApplicationContextInitializer.class);
    
    private static final String CONFIG_FILENAME = "opentsdb.conf";
    
    public static final String PROPERTY_SOURCE_NAME = "TSDBCONF";
    
    /** Locations to search for override config file.  These are treated as {@link Resource} expressions.
     * Note that {@link #JSON_CONFIG_FILENAME} is appended directly to these locations, so make sure they
     * end with a slash */
    private static final String[] OVERRIDE_SEARCH_LOCATIONS;
    
    static {
        final String userHome = System.getProperty("user.home");
        
        OVERRIDE_SEARCH_LOCATIONS = new String[] {
                // The directory where the JVM started.
                "file://",
                // User's home directory (for Tomcat this is like: /var/lib/tomcat6/conf)
                String.format("file://%s/conf/", userHome),
                // User's home directory.
                String.format("file://%s/", userHome)
        };
    }
    
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(applicationContext.getClassLoader());
        
        try {
            final TsdbConfigPropertySource config = loadTsdbConfig(resolver);
            log.info("Application config is using {}", config.getName());
            applicationContext.getEnvironment().getPropertySources().addFirst(config);
        } catch (IOException e) {
            throw new BeanInitializationException("Unable to load a TSDB config file!", e);
        }
    }
    
    private TsdbConfigPropertySource loadTsdbConfig(final ResourcePatternResolver resolver) throws IOException {
        Resource configResource = null;
        
        for (final String location : OVERRIDE_SEARCH_LOCATIONS) {
            final String fullLoc = String.format("%s%s", location, CONFIG_FILENAME);
            log.debug("Searching for TSDB config in {}", fullLoc);
            
            final Resource res = resolver.getResource(fullLoc);
            if (res != null && res.exists()) {
                configResource = res;
                log.info("Found TSDB config file using {} ", fullLoc);
                break;
            }
        }
        
        if (configResource == null) {
            return new TsdbConfigPropertySource(PROPERTY_SOURCE_NAME, new Config(true));
        } else if (configResource.isReadable()) {
            return new TsdbConfigPropertySource(PROPERTY_SOURCE_NAME, new Config(configResource.getFile().getAbsolutePath()));
        } else {
            throw new IllegalStateException("Unable to locate any TSDB config files!");
        }
    }
}
