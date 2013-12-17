package net.opentsdb.contrib.tsquare.support;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.ConfigurableWebApplicationContext;

import com.google.common.collect.Lists;

/**
 * @author James Royalty (jroyalty) <i>[Aug 5, 2013]</i>
 */
public class TsApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableWebApplicationContext> {
    private static final Logger log = LoggerFactory.getLogger(TsApplicationContextInitializer.class);
    
    public static final String APP_CONTEXT_RESOURCE = "classpath:app-context.xml";
    public static final String USER_CONTEXT_FILENAME = "tsquare-user-config.xml";
    
    /** Locations to search for override config file.  These are treated as {@link Resource} expressions.
     * Note that {@link #USER_CONTEXT_FILENAME} is appended directly to these locations, so make sure they
     * end with a slash */
    private static final String[] OVERRIDE_SEARCH_LOCATIONS;
    
    static {
        final String userHome = System.getProperty("user.home");
        
        OVERRIDE_SEARCH_LOCATIONS = new String[] {
                // Look on the classpath first.
                "classpath:",
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
    public void initialize(final ConfigurableWebApplicationContext applicationContext) {
//        Preconditions.checkState(applicationContext.getConfigLocations() == null || applicationContext.getConfigLocations().length == 0,
//                "Config locations have already been set on %s. Make sure you aren't setting a 'contextConfigLocation' in web.xml or elsewhere.", applicationContext);
        
        final List<String> contextResources = Lists.newArrayListWithExpectedSize(2);
        
        // The application's context config is required, and must be loaded FIRST.
        log.info("Using application context {}", APP_CONTEXT_RESOURCE);
        contextResources.add(APP_CONTEXT_RESOURCE);
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(applicationContext.getClassLoader());
            Resource userConfigRes = firstExistingUserConfig(resolver);
            
            if (userConfigRes != null) {
                log.info("Adding user config from {}", userConfigRes);
                contextResources.add(userConfigRes.getURI().toString());
            } else {
                log.info("User config not found. This config is not required so continue with initialization.");
            }
        } catch (IOException e) {
            throw new ApplicationContextException("Unable to resolve user config file.", e);
        }
        
        applicationContext.setConfigLocations(contextResources.toArray(new String[] {}));
    }
    
    private Resource firstExistingUserConfig(final ResourcePatternResolver resolver) throws IOException {
        Resource configResource = null;
        
        for (final String location : OVERRIDE_SEARCH_LOCATIONS) {
            final String fullLoc = String.format("%s%s", location, USER_CONTEXT_FILENAME);
            log.debug("Searching for user config in {}", fullLoc);
            
            final Resource res = resolver.getResource(fullLoc);
            if (res != null && res.exists()) {
                configResource = res;
                break;
            }
        }
        
        // This might be null, but that's okay because this config isn't required.
        return configResource;
    }
}
