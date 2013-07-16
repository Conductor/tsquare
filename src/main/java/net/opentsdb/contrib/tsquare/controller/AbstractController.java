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
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.opentsdb.contrib.tsquare.DateTimeExpressionParser;
import net.opentsdb.contrib.tsquare.TsdbManager;
import net.opentsdb.contrib.tsquare.support.ResponseStream;
import net.opentsdb.core.DataPoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;

/**
 * @author James Royalty (jroyalty) <i>[Jun 11, 2013]</i>
 */
public abstract class AbstractController {
    @Autowired
    private TsdbManager tsdbManager;
    private int defaultResponseBufferSize = 1024 * 1000;
    
    protected QueryDurationParams parseDurations(final String fromDateTimeExpr, final String untilDateTimeExpr) {
        // Relative times are computed based on this NOW time.
        final long nowMillis = System.currentTimeMillis();
        
        // From date/time is required.
        final long fromMillis = new DateTimeExpressionParser()
            .setBaseTimeMillis(nowMillis)
            .setPositiveOffset(false)
            .parseRequired(fromDateTimeExpr);
        
        // Until time is optional and defaults to NOW.
        final long untilMillis;
        if (Strings.isNullOrEmpty(untilDateTimeExpr)) {
            untilMillis = nowMillis;
        } else {
            untilMillis = new DateTimeExpressionParser()
                    .setBaseTimeMillis(nowMillis)
                    .setPositiveOffset(false)
                    .parseRequired(untilDateTimeExpr);
        }
        
        return new QueryDurationParams(nowMillis, fromMillis, untilMillis);
    }
    
    protected void writeGraphiteLikeJsonFormat(final Collection<QueryMetaData> queries, final DataPointsWriter writer, final JsonGenerator json, final WebRequest webRequest) 
            throws JsonGenerationException, IOException {
        // Generate a response wrapped in the provided JSONP wrapper?
        final boolean jsonpResponse = webRequest.getParameterMap().containsKey("jsonp");
        
        if (jsonpResponse) {
            final String jsonpValue = webRequest.getParameter("jsonp");
            if (!Strings.isNullOrEmpty(jsonpValue)) {
                json.writeRaw(jsonpValue);
            }

            json.writeRaw('('); // START of jsonp response wrapper.
        }

        json.writeStartArray(); // START of response array

        // Write each series and data points...
        for (final QueryMetaData qmeta : queries) {
            // TODO: Could dispatch these to a shared thread pool. 
            // Run them serially now.
            final DataPoints[] series = qmeta.getQuery().run();
            
            for (final DataPoints points : series) {
                writer.write(qmeta.getMetric(), points, json);
            }
        }

        json.writeEndArray(); // END of response array

        if (jsonpResponse) {
            json.writeRaw(')'); // END of jsonp response wrapper.
        }
    }
    
    /**
     * Render a JSON view using the given object.
     * 
     * @param viewData
     * @return
     */
    protected ModelAndView jsonSingleObjectView(final Object viewData) {
        ModelAndView mv = new ModelAndView("jsonSingleObject");
        mv.addObject("object", viewData);
        return mv;
    }
    
    /**
     * Writes the given stream to the response.  Does not close the {@link HttpServletResponse} output.
     * 
     * @param stream
     * @param response
     * @throws IOException
     */
    protected void copyJsonToResponse(final ResponseStream stream, final HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setContentLength((int) stream.getNumBytesWritten());
        InputStream from = null;
        ServletOutputStream servletOut = null;
        
        try {
            from = stream.getSupplier().getInput();
            servletOut = response.getOutputStream();
            ByteStreams.copy(from, servletOut);
        } finally {
            Closeables.close(from, true);
        }
    }

    /**
     * @return shared {@link TsdbManager} instance.
     */
    public TsdbManager getTsdbManager() {
        return tsdbManager;
    }
    
    /**
     * For methods that stream their responses to the client, this is the suggested
     * buffer size to use.
     * 
     * @return buffer size in bytes
     */
    public int getDefaultResponseBufferSize() {
        return defaultResponseBufferSize;
    }
    
    /**
     * @param responseBufferSize number of bytes of the JSON response to buffer in memory.
     * Bytes written beyond this limit are spilled a temporary file on disk.
     */
    public void setDefaultResponseBufferSize(int responseBufferSize) {
        this.defaultResponseBufferSize = responseBufferSize;
    }
}
