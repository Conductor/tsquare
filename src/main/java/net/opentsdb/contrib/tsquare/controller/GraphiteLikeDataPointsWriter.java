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
package net.opentsdb.contrib.tsquare.controller;

import java.io.IOException;
import java.util.Map;

import net.opentsdb.contrib.tsquare.Metric;
import net.opentsdb.contrib.tsquare.support.DataPointsAsDoubles;
import net.opentsdb.core.DataPoint;
import net.opentsdb.core.DataPoints;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Write the data points to the JSON response. This method should contribute one
 * JSON object like:
 * <pre>
 * {
 *  "target": "METRIC NAME",
 *  "datapoints": [
 *      [value_1, timestamp_1]
 *      . . .
 *      , [value_N, timestamp_N]
 *  ]
 * }
 * </pre>
 * 
 * @author James Royalty (jroyalty) <i>[Jun 26, 2013]</i>
 */
class GraphiteLikeDataPointsWriter implements DataPointsWriter {
    private boolean summarize = false;
    private boolean includeAllTags = false;
    private boolean includeAggregatedTags = false;
    
    /**
     * Creates a writer that produces the Graphite format, without any
     * extensions or extra data.
     */
    public GraphiteLikeDataPointsWriter() {
    }
    
    @Override
    public void write(final Metric metric, final DataPoints points, final JsonGenerator jsonObject) 
            throws JsonGenerationException, IOException {
        jsonObject.writeStartObject();
        jsonObject.writeStringField("target", points.metricName());
        
        if (includeAllTags) {
            jsonObject.writeArrayFieldStart("tags");
            for (final Map.Entry<String, String> entry : points.getTags().entrySet()) {
                jsonObject.writeStartObject();
                jsonObject.writeStringField("key", entry.getKey());
                jsonObject.writeStringField("value", entry.getValue());
                jsonObject.writeEndObject();
            }
            jsonObject.writeEndArray();
        }
        
        if (includeAggregatedTags) {
            jsonObject.writeArrayFieldStart("aggregatedTags");
            for (final String tag : points.getAggregatedTags()) {
                jsonObject.writeString(tag);
            }
            jsonObject.writeEndArray();
        }
        
        if (summarize) {
            final DataPointsAsDoubles doubles = new DataPointsAsDoubles(points);
            final double aggValue = metric.getAggregator().runDouble(doubles);
            jsonObject.writeNumberField("summarizedValue", aggValue);
        } else {
            jsonObject.writeArrayFieldStart("datapoints");

            for (final DataPoint p : points) {
                jsonObject.writeStartArray();

                if (p.isInteger()) {
                    jsonObject.writeNumber(p.longValue());
                } else {
                    jsonObject.writeNumber(p.doubleValue());
                }

                jsonObject.writeNumber(p.timestamp());
                jsonObject.writeEndArray();
            }

            jsonObject.writeEndArray();
        }
        
        jsonObject.writeEndObject();
    }

    public GraphiteLikeDataPointsWriter setIncludeAllTags(boolean includeAllTags) {
        this.includeAllTags = includeAllTags;
        return this;
    }

    public GraphiteLikeDataPointsWriter setIncludeAggregatedTags(boolean includeAggregatedTags) {
        this.includeAggregatedTags = includeAggregatedTags;
        return this;
    }

    public GraphiteLikeDataPointsWriter setSummarize(boolean summarize) {
        this.summarize = summarize;
        return this;
    }
}
