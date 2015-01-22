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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import net.opentsdb.contrib.tsquare.support.DataPointsAsDoubles;
import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;
import net.opentsdb.core.Aggregators;
import net.opentsdb.core.DataPoint;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Write data in Graphite's "json" format.  See: http://graphite.readthedocs.org/en/latest/render_api.html#json
 * Note that this implementation can include certain extensions to that format, 
 * but by default, it's output is consistent with Graphite's.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 26, 2013]</i>
 */
public class GraphiteJsonResponseWriter extends AbstractJsonResponseWriter implements SingleSeriesWriter {
    private boolean summarize = false;
    private boolean includeAllTags = false;
    private boolean includeAggregatedTags = false;
    
    public GraphiteJsonResponseWriter(final boolean millisecondResolution) {
        super(millisecondResolution);
    }
    
    @Override
    public void beginResponse(ResponseContext context) throws IOException {
        super.beginResponse(context);
        
        // START of response array 
        getJsonGenerator(context).writeStartArray(); 
    }
    
    @Override
    public void write(final AnnotatedDataPoints annotatedPoints, final ResponseContext context) throws IOException {
        final JsonGenerator jsonGenerator = getJsonGenerator(context);
        
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("target", annotatedPoints.getDataPoints().metricName());
        
        if (includeAllTags) {
            jsonGenerator.writeArrayFieldStart("tags");
            for (final Map.Entry<String, String> entry : annotatedPoints.getDataPoints().getTags().entrySet()) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeStringField("key", entry.getKey());
                jsonGenerator.writeStringField("value", entry.getValue());
                jsonGenerator.writeEndObject();
            }
            jsonGenerator.writeEndArray();
        }
        
        if (includeAggregatedTags) {
            jsonGenerator.writeArrayFieldStart("aggregatedTags");
            for (final String tag : annotatedPoints.getDataPoints().getAggregatedTags()) {
                jsonGenerator.writeString(tag);
            }
            jsonGenerator.writeEndArray();
        }
        
        if (summarize) {
            final DataPointsAsDoubles doubles = new DataPointsAsDoubles(annotatedPoints.getDataPoints());
            final double aggValue = Aggregators.SUM.runDouble(doubles);
            jsonGenerator.writeNumberField("summarizedValue", aggValue);
        } else {
            jsonGenerator.writeArrayFieldStart("datapoints");

            for (final DataPoint p : annotatedPoints.getDataPoints()) {
                jsonGenerator.writeStartArray();

                if (p.isInteger()) {
                    jsonGenerator.writeNumber(p.longValue());
                } else {
                    jsonGenerator.writeNumber(p.doubleValue());
                }

                if (isMillisecondResolution()) {
                    jsonGenerator.writeNumber(p.timestamp());
                } else {
                    jsonGenerator.writeNumber(TimeUnit.MILLISECONDS.toSeconds(p.timestamp()));
                }
                
                jsonGenerator.writeEndArray();
            }

            jsonGenerator.writeEndArray();
        }
        
        jsonGenerator.writeEndObject();
    }
    
    @Override
    public void endResponse(final ResponseContext context) throws IOException {
        // END of response array
        getJsonGenerator(context).writeEndArray();
        
        // END of super class response (which might include JSONP wrapper).
        super.endResponse(context);
        
        getJsonGenerator(context).flush();
    }
    
    public GraphiteJsonResponseWriter setIncludeAllTags(boolean includeAllTags) {
        this.includeAllTags = includeAllTags;
        return this;
    }

    public GraphiteJsonResponseWriter setIncludeAggregatedTags(boolean includeAggregatedTags) {
        this.includeAggregatedTags = includeAggregatedTags;
        return this;
    }

    public GraphiteJsonResponseWriter setSummarize(boolean summarize) {
        this.summarize = summarize;
        return this;
    }
}
