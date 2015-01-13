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

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hbase.async.Scanner;
import org.springframework.util.ReflectionUtils;

import net.opentsdb.core.DataPoint;
import net.opentsdb.core.TSDB;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

/**
 * @author James Royalty (jroyalty) <i>[Jun 18, 2013]</i>
 */
public final class TsWebUtils {
    public static final Charset CHARSET;
    
    static {
        try {
            // The CHARSET is defined on both TSDB and the UniqueId classes.
            // They are the same as of 2.0.1 and we prefer the one on TSDB.
            Field f = ReflectionUtils.findField(TSDB.class, "CHARSET");
            ReflectionUtils.makeAccessible(f);
            CHARSET = (Charset) f.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    
    public static final byte[] toBytes(final String s) {
        return s.getBytes(CHARSET);
    }
    
    public static final String toString(final byte[] b) {
        return new String(b, CHARSET);
    }
    
    public static final double asDouble(final DataPoint point) {
        if (point.isInteger()) {
            return (double) point.longValue();
        } else {
            return point.doubleValue();
        }
    }
    
    public static final Double asDoubleObject(final DataPoint point) {
        final double value;
        
        if (point.isInteger()) {
            value = (double) point.longValue();
        } else {
            value = point.doubleValue();
        }
        
        return Double.valueOf(value);
    }
    
    /**
     * Converts the given wildcard expression to a regular expressions.  The resulting
     * regular expression is suitable for use in {@link Scanner#setKeyRegexp(String)}.
     * The regular expression is case insensitive.
     * 
     * @param wildcard
     * @return
     */
    public static String wildcardToRegex(final String wildcard) {
        final StringBuilder regex = new StringBuilder();
        
        // Wildcards ignore case, by default.
        // This does what net.opentsdb.tools.UidManager.grep() does.
        regex.append("(?i)");
        
        // Anchor start of pattern.
        regex.append("^");
        
        for (int i=0; i<wildcard.length(); i++) {
            final char ch = wildcard.charAt(i);
            switch (ch) {
            case '*': // Greedy match, .*
                regex.append(".*");
                break;
            case '?': // Single char match.
                regex.append(".");
                break;
             
            // Escape regex operators...
            case '.':
            case '{':
            case '}':
            case '^':
            case '$':
            case '[':
            case ']':
                regex.append("\\").append(ch);
                break;
                
            // By default, echo this char as a literal match.
            default:
                regex.append(ch);
                break;
            }
        }
        
        // Anchor end.
        regex.append("$");
        
        return regex.toString();
    }
    
    /**
     * @return an {@link ObjectMapper} configured for the most common parsing
     * use cases in the application.
     */
    public static ObjectMapper newObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(Feature.ALLOW_COMMENTS, true);
        return mapper;
    }
}
