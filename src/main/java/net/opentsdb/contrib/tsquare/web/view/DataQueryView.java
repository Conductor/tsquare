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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;
import net.opentsdb.contrib.tsquare.web.AnnotatedDataQuery;
import net.opentsdb.contrib.tsquare.web.DataQueryModel;
import net.opentsdb.core.DataPoints;

import org.springframework.web.servlet.View;

/**
 * Special view for streaming query result data from a {@link DataQueryModel}.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 31, 2013]</i>
 */
public final class DataQueryView implements View {
    @Override
    public void render(final Map<String, ?> modelMap, final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final DataQueryModel modelObj = new DataQueryModel(modelMap);
        final DataQueryResponseWriter writer = modelObj.getResponseWriter();
        final ResponseContext context = new ResponseContext(request, response);
        
        if (writer instanceof SteppedSeriesWriter) {
            executeSteppedSeries((SteppedSeriesWriter) writer, modelObj, context);
        } else if (writer instanceof SingleSeriesWriter) {
            executeSingleSeries((SingleSeriesWriter) writer, modelObj, context);
        } else {
            throw new IllegalArgumentException("Unsupported writer: " + writer);
        }
    }
    
    private void executeSingleSeries(final SingleSeriesWriter singleSeriesWriter, final DataQueryModel modelObj, final ResponseContext context) throws IOException {
        try {
            singleSeriesWriter.beginResponse(context);

            for (final AnnotatedDataQuery dataQuery : modelObj.getQueries()) {
                final DataPoints[] result = dataQuery.getQuery().run();
                for (final DataPoints series : result) {
                    singleSeriesWriter.write(new AnnotatedDataPoints(dataQuery.getMetric(), series), context);
                }
            }

            singleSeriesWriter.endResponse(context);
        } finally {
            singleSeriesWriter.close(context);
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
