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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

/**
 * @author James Royalty (jroyalty) <i>[Jun 17, 2013]</i>
 */
public class TsWebApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(TsWebApplicationContextInitializer.class);
    
    public static final String JSON_CONFIG_FILENAME = "tsquare_config.json";
    
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
                String.format("file://%s/", userHome),
                // System-wide TSDB install dir.
                "file:///usr/local/share/opentsdb/"
        };
    }
    
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
        final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(applicationContext.getClassLoader());
        
        final JsonPropertySource defaultConfig = loadRequiredDefaultConfig(resolver);
        applicationContext.getEnvironment().getPropertySources().addFirst(defaultConfig);
        log.info("Default config is using {}", defaultConfig.getName());
        
        JsonPropertySource overrideConfig = null;
        try {
            overrideConfig = loadOverrideConfig(resolver);
            applicationContext.getEnvironment().getPropertySources().addBefore(defaultConfig.getName(), overrideConfig);
            log.info("Override config is using {}", overrideConfig.getName());
        } catch (Exception e) {
            log.warn("Unable to load override config; will continue anyway.  Error: {}", e.getMessage());
        }
    }
    
    private JsonPropertySource loadRequiredDefaultConfig(final ResourcePatternResolver resolver) {
        final String loc = String.format("classpath:%s", JSON_CONFIG_FILENAME);
        final Resource res = resolver.getResource(loc);
        Preconditions.checkState(res.exists(), "Unable to load default JSON config from %s", loc);
        
        try {
            return newJsonPropertySource(res);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to parse JSON in " + res.getDescription(), e);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load JSON from " + res.getDescription(), e);
        }
    }
    
    private JsonPropertySource loadOverrideConfig(final ResourcePatternResolver resolver) throws JsonProcessingException, IOException {
        Resource configResource = null;
        
        for (final String location : OVERRIDE_SEARCH_LOCATIONS) {
            final String fullLoc = String.format("%s%s", location, JSON_CONFIG_FILENAME);
            log.debug("Searching for config in {}", fullLoc);
            
            final Resource res = resolver.getResource(fullLoc);
            if (res != null && res.exists()) {
                configResource = res;
                log.info("Found JSON config file using {} ", fullLoc);
                break;
            }
        }
        
        // This config is not required anyway.
        if (configResource == null) {
            return null;
        } else {
            return newJsonPropertySource(configResource);
        }
    }
    
    private JsonPropertySource newJsonPropertySource(final Resource resource) throws JsonProcessingException, IOException {
        ObjectMapper mapper = TsWebUtils.newObjectMapper();
        JsonNode root = mapper.readTree(resource.getURL());
        JsonPropertySource ps = new JsonPropertySource(resource.getDescription(), root);
        return ps;
    }
}
