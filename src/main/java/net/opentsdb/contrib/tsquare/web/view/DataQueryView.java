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
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;
import net.opentsdb.contrib.tsquare.web.AnnotatedDataQuery;
import net.opentsdb.contrib.tsquare.web.DataQueryModel;
import net.opentsdb.core.DataPoints;

import org.springframework.web.servlet.view.AbstractView;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

/**
 * Special view for streaming query result data from a {@link DataQueryModel}.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 31, 2013]</i>
 */
public final class DataQueryView extends AbstractView {
    
    @Override
    protected void renderMergedOutputModel(final Map<String, Object> modelMap, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DataQueryModel modelObj = new DataQueryModel(modelMap);
        final DataQueryResponseWriter writer = modelObj.getResponseWriter();
        final ResponseContext context = new ResponseContext(request, response);
        
        if (writer instanceof SteppedSeriesWriter) {
            executeSteppedSeries((SteppedSeriesWriter) writer, modelObj, context);
        } else if (writer instanceof GroupedSeriesWriter) {
            executeGroupedSeries((GroupedSeriesWriter) writer, modelObj, context);
        } else if (writer instanceof SingleSeriesWriter) {
            executeSingleSeries((SingleSeriesWriter) writer, modelObj, context);
        } else {
            throw new IllegalArgumentException("Unsupported writer: " + writer);
        }
    }
    
    private void executeSingleSeries(final SingleSeriesWriter singleSeriesWriter, final DataQueryModel modelObj, final ResponseContext context) throws IOException {
        Exception ex = null;
        
        try {
            singleSeriesWriter.beginResponse(context);

            for (final AnnotatedDataQuery dataQuery : modelObj.getQueries()) {
                final DataPoints[] result = dataQuery.getQuery().run();
                for (final DataPoints series : result) {
                    AnnotatedDataPoints annPoints = new AnnotatedDataPoints(
                            dataQuery.getMetric(),
                            Range.closed(dataQuery.getQuery().getStartTime(), dataQuery.getQuery().getEndTime()),
                            series);
                    
                    singleSeriesWriter.write(annPoints, context);
                }
            }

            singleSeriesWriter.endResponse(context);
        } catch (IOException e) {
            ex = e;
            throw e;
        } catch (RuntimeException e) {
            ex = e;
            throw e;
        } finally {
            if (ex != null) {
                singleSeriesWriter.onError(context, ex);
            }
        }
    }
    
    private void executeGroupedSeries(final GroupedSeriesWriter groupedSeriesWriter, final DataQueryModel modelObj, final ResponseContext context) throws IOException {
        Exception ex = null;
        
        try {
            groupedSeriesWriter.beginResponse(context);
            
            final Multimap<String, AnnotatedDataPoints> groups = LinkedListMultimap.create();

            for (final AnnotatedDataQuery dataQuery : modelObj.getQueries()) {
                final DataPoints[] result = dataQuery.getQuery().run();
                for (final DataPoints series : result) {
                    AnnotatedDataPoints points = new AnnotatedDataPoints(
                            dataQuery.getMetric(), 
                            Range.closed(dataQuery.getQuery().getStartTime(), dataQuery.getQuery().getEndTime()), 
                            series);
                    groups.put(series.metricName(), points);
                }
            }
            
            for (final String metricName : groups.keySet()) {
                Collection<AnnotatedDataPoints> points = groups.get(metricName);
                groupedSeriesWriter.write(points, context);
            }

            groupedSeriesWriter.endResponse(context);
        } catch (IOException e) {
            ex = e;
            throw e;
        } catch (RuntimeException e) {
            ex = e;
            throw e;
        } finally {
            if (ex != null) {
                groupedSeriesWriter.onError(context, ex);
            }
        }
    }
    
    private void executeSteppedSeries(final SteppedSeriesWriter steppedSeriesWriter, final DataQueryModel modelObj, final ResponseContext context) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet!");
//        try {
//            steppedSeriesWriter.beginResponse(context);
//            
//            for (final AnnotatedDataQuery dataQuery : modelObj.getQueries()) {
//                final DataPoints[] result = dataQuery.getQuery().run();
//            }
//
//            steppedSeriesWriter.endResponse(context);
//        } finally {
//            steppedSeriesWriter.close(context);
//        }
    }
    
    @Override
    public final String getContentType() {
        // The writer set the content type.
        return null;
    }
}
