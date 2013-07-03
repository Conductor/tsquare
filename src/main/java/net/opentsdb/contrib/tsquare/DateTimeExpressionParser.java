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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * A flexible parser that support a variety of absolute and relative date/time expressions.
 * 
 * @author James Royalty (jroyalty) <i>[Jun 10, 2013]</i>
 */
public class DateTimeExpressionParser {
    private static final String[] DEFAULT_DATE_FORMATS = {
        "HH:mm_yyyyMMdd",       // Graphite
        "yyyyMMdd",             // Graphite
        "MM/dd/yy",             // Graphite
        "yyyy/MM/dd-HH:mm:ss"   // TSDB
    };
    
    public static final String DURATION_REGEX = "(\\d*)(sec|min|hour|day|week|milli|ms)(s?)";
    public static final String GRAPHITE_DURATION_REGEX = "-(\\d+)(s|min|minutes|h|d|w|mon|y)";
    public static final String TSDB_DURATION_REGEX = "(\\d+)(s|m|h|d|w|y)(-ago)?";
    
    private static final Pattern[] RELATIVE_PATTERNS = {
        Pattern.compile(GRAPHITE_DURATION_REGEX, Pattern.CASE_INSENSITIVE),
        Pattern.compile(TSDB_DURATION_REGEX, Pattern.CASE_INSENSITIVE),
        Pattern.compile(DURATION_REGEX, Pattern.CASE_INSENSITIVE)
    };
    
    private long baseTimeMillis;
    private boolean positiveOffset = false;
    
    public DateTimeExpressionParser() {
        this.baseTimeMillis = System.currentTimeMillis();
    }
    
    public long parse(final String dateTimeExpression) {
        for (final Pattern p : RELATIVE_PATTERNS) {
            final Matcher m = p.matcher(dateTimeExpression);
            if (m.matches()) {
                return parseRelativeExpression(m, dateTimeExpression);
            }
        }
        
        for (final String formatString : DEFAULT_DATE_FORMATS) {
            try {
                final SimpleDateFormat sdf = new SimpleDateFormat(formatString);
                // We want exact matches for our patterns.
                sdf.setLenient(false);
                Date d = sdf.parse(dateTimeExpression);
                return d.getTime();
            } catch (ParseException e) {
                continue;
            }
        }
        
        return -1L;
    }
    
    public long parseRequired(final String dateTimeExpression) throws IllegalArgumentException {
        final long value = parse(dateTimeExpression);
        Preconditions.checkArgument(value > 0, "Invalid date/time expression: %s", dateTimeExpression);
        return value;
    }
    
    private long parseRelativeExpression(final Matcher matcher, final String dateTimeExpression) {
        String multiplierString = matcher.group(1);
        long multiplier = Strings.isNullOrEmpty(multiplierString) ? 1 : Long.parseLong(multiplierString);
        
        TimeUnit units;
        final String unitString = matcher.group(2);
        
        if ("sec".equalsIgnoreCase(unitString) || "s".equalsIgnoreCase(unitString)) {
            units = TimeUnit.SECONDS;
        } else if ("min".equalsIgnoreCase(unitString) || "m".equalsIgnoreCase(unitString) || "minutes".equalsIgnoreCase(unitString)) {
            units = TimeUnit.MINUTES;
        } else if ("hour".equalsIgnoreCase(unitString) || "h".equalsIgnoreCase(unitString)) {
            units = TimeUnit.HOURS;
        } else if ("day".equalsIgnoreCase(unitString) || "d".equalsIgnoreCase(unitString)) {
            units = TimeUnit.DAYS;
        } else if ("week".equalsIgnoreCase(unitString) || "w".equalsIgnoreCase(unitString)) {
            units = TimeUnit.DAYS;
            multiplier *= 7;
        } else if ("mon".equalsIgnoreCase(unitString)) { // GRAPHITE_DURATION_PATTERN only
            units = TimeUnit.DAYS;
            // Months have 30 days:  http://graphite.readthedocs.org/en/0.9.10/render_api.html#from-until
            multiplier *= 30;
        } else if ("y".equalsIgnoreCase(unitString)) {
            units = TimeUnit.DAYS;
            // Years have 365 days:  http://graphite.readthedocs.org/en/0.9.10/render_api.html#from-until
            multiplier *= 365;
        } else if ("milli".equalsIgnoreCase(unitString) || "ms".equalsIgnoreCase(unitString)) { // DURATION_PATTERN only
            units = TimeUnit.MILLISECONDS;
        } else {
            throw new IllegalArgumentException("Invalid Graphite-like relative expression: " + dateTimeExpression);
        }
        
        final long rel = units.toMillis(multiplier);
        
        if (positiveOffset) {
            return baseTimeMillis + rel;
        } else {
            return baseTimeMillis - rel;
        }
    }

    /**
     * Default value for <strong>time now</strong>, if only a date format is given. This can be
     * set to zero if want to parse a relative-only date/time format.
     * 
     * @param baseTimeMillis
     * @return
     */
    public DateTimeExpressionParser setBaseTimeMillis(long baseTimeMillis) {
        this.baseTimeMillis = baseTimeMillis;
        return this;
    }

    /**
     * Are relative expressions postive or negative offsets from {@code baseTimeMillis}?
     * 
     * @param positiveOffset
     * @return
     */
    public DateTimeExpressionParser setPositiveOffset(boolean positiveOffset) {
        this.positiveOffset = positiveOffset;
        return this;
    }
}
