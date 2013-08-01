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
package net.opentsdb.contrib.tsquare.web.view;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import net.opentsdb.contrib.tsquare.support.TsWebUtils;
import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;
import net.opentsdb.core.DataPoint;
import net.opentsdb.core.DataPoints;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

/**
 * Write data in Graphite's "raw" format.  See: http://graphite.readthedocs.org/en/latest/render_api.html#raw
 * 
 * @author James Royalty (jroyalty) <i>[Jul 30, 2013]</i>
 */
public class GraphiteRawResponseWriter implements SingleSeriesWriter {
    @Override
    public void beginResponse(final ResponseContext context) throws IOException {
        context.getResponse().setContentType("text/plain");
    }
    
    @Override
    public void write(final AnnotatedDataPoints annotatedPoints, final ResponseContext context) throws IOException {
        final DataPoints points = annotatedPoints.getDataPoints();
        
        long maxTimestamp = Long.MIN_VALUE;
        long minTimestamp = Long.MAX_VALUE;
        
        long lastTimestamp = 0;
        long diffSum = 0;
        long totalPoints = 0;
        
        // XXX: Maybe just iterate over DataPoints again instead of caching these?
        final List<Double> values = Lists.newArrayListWithCapacity(points.size());
        
        for (final DataPoint p : points) {
            final long currentTimestamp = p.timestamp();
            
            // Find the max/min timestamps; we need this for part of the raw response.
            maxTimestamp = Math.max(maxTimestamp, currentTimestamp);
            minTimestamp = Math.min(minTimestamp, currentTimestamp);
            
            // The Graphite raw format requires a series step value, which has on
            // corollary in OpenTSDB.  So we estimate it.  Sum the differences 
            // between consecutive timestamps in order to compute average separation.  
            if (lastTimestamp > 0) { // ... skips the first iteration.
                long diff = currentTimestamp - lastTimestamp;
                if (diff >= 0) { // ... accounts for out-of-order results.
                    diffSum += diff;
                    totalPoints++;
                }
            }
            
            lastTimestamp = p.timestamp();
            values.add(TsWebUtils.asDoubleObject(p));
        }
        
        final PrintWriter writer = context.getResponse().getWriter();
        writer.print(points.metricName());
        writer.print(',');
        writer.print(minTimestamp);
        writer.print(',');
        writer.print(maxTimestamp);
        writer.print(',');
        writer.print((diffSum/totalPoints));
        writer.print('|');
        writer.println(Joiner.on(',').join(values));
    }

    @Override
    public void endResponse(final ResponseContext context) throws IOException {
        context.getResponse().getWriter().flush();
    }

    @Override
    public void close(final ResponseContext context) {
        // Do nothing.
    }
}
