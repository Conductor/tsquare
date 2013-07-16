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
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import net.opentsdb.contrib.tsquare.Metric;
import net.opentsdb.contrib.tsquare.MetricParser;
import net.opentsdb.contrib.tsquare.QueryCallback;
import net.opentsdb.contrib.tsquare.Uid;
import net.opentsdb.contrib.tsquare.UidQuery;
import net.opentsdb.contrib.tsquare.support.ResponseStream;
import net.opentsdb.contrib.tsquare.support.TsWebUtils;
import net.opentsdb.core.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Closeables;

/**
 * @author James Royalty (jroyalty) <i>[Jun 20, 2013]</i>
 */
@Controller
@RequestMapping("/ext")
public class ExtendedApiController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(ExtendedApiController.class);
    
    @RequestMapping(value = "/grep", method = RequestMethod.GET)
    public void grep(
            @RequestParam(required=false, defaultValue="") String type,
            @RequestParam(required=false, defaultValue="wildcard") String method,
            @RequestParam(required=true) String q,
            final HttpServletResponse servletResponse) throws IOException {
        
        if (log.isInfoEnabled()) {
            log.info("Suggest {} using {} expression: {}", type, method, q);
        }
        
        // Do we have a valid type? Note that an empty "type" is valid.
        if (!Strings.isNullOrEmpty(type)) {
            Preconditions.checkArgument(getTsdbManager().getKnownUidKinds().contains(type), "Unknown type: %s", type);
        }
        
        // We can only query hbase using regex, so convert a wildcard query into
        // a regex if necessary.
        final String regex;
        if ("wildcard".equalsIgnoreCase(method)) {
            regex = TsWebUtils.wildcardToRegex(q);
            log.debug("Converted wildcard expression {} to regex: {}", q, regex);
        } else {
            regex = q;
        }
        
        final UidQuery query = getTsdbManager().newUidQuery();
        query.setRegex(regex);
        
        if (Strings.isNullOrEmpty(type)) {
            query.includeAllKinds();
        } else {
            query.includeKind(type);
        }
        
        ResponseStream stream = null;
        try {
            stream = new ResponseStream(getDefaultResponseBufferSize());
            final JsonGenerator json = new JsonFactory().createJsonGenerator(stream);
            json.writeStartArray();
            
            query.run(new QueryCallback<Uid>() {
                @Override
                public boolean onResult(final Uid resultObject) {
                    try {
                        json.writeString(resultObject.getName());
                        return true;
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Unable to serialize " + resultObject + " to JSON", e);
                    }
                }
            });
            
            json.writeEndArray();
            json.flush();
            stream.close();
            copyJsonToResponse(stream, servletResponse);
        } finally {
            ResponseStream.releaseQuietly(stream);
        }
    }
    
    @RequestMapping(value = "/kinds", method = RequestMethod.GET) 
    public ModelAndView kinds() throws IOException {
        return jsonSingleObjectView(getTsdbManager().getKnownUidKinds());
    }
    
    @RequestMapping(value = "/q", method = RequestMethod.GET)
    public void query(
            @RequestParam(required=true) String[] m,
            @RequestParam(required=true) String start,
            @RequestParam(required=false) String end,
            @RequestParam(required=false, defaultValue="false") boolean summarize,
            final WebRequest webRequest,
            final HttpServletResponse servletResponse) throws IOException {
        
        final QueryDurationParams durationParams = parseDurations(start, end);
        if (log.isInfoEnabled()) {
            log.info("{}", durationParams);
        }
        
        // Prepare queries...
        final MetricParser parser = getTsdbManager().newMetricParser();
        final List<QueryMetaData> queries = Lists.newArrayListWithCapacity(m.length);
        
        for (final String t : m) {
            final Query q = getTsdbManager().newMetricsQuery();
            durationParams.contributeToQuery(q);
            
            final Metric metric = parser.parseMetric(t);
            if (summarize) {
                Preconditions.checkState(metric.getAggregator() != null, "A metric-level aggregator is required when 'summarize' is enabled.  Metric: {}", t);
            }
            metric.contributeToQuery(q);
            
            queries.add(new QueryMetaData(metric, q));
            
            log.info("Added {} to query", metric);
        }
        
        servletResponse.setBufferSize(getDefaultResponseBufferSize());
        servletResponse.setContentType("application/json");
        
        ServletOutputStream out = servletResponse.getOutputStream();
        
        try {
            final JsonGenerator json = new JsonFactory().createJsonGenerator(out);
            final GraphiteLikeDataPointsWriter writer = new GraphiteLikeDataPointsWriter()
                .setIncludeAggregatedTags(true)
                .setIncludeAllTags(true)
                .setSummarize(summarize);
            writeGraphiteLikeJsonFormat(queries, writer, json, webRequest);
            json.flush();
        } finally {
            Closeables.close(out, false);
        }
    }
}
