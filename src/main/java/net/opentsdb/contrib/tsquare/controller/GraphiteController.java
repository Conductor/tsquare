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
import java.util.Set;

import net.opentsdb.contrib.tsquare.Metric;
import net.opentsdb.contrib.tsquare.MetricParser;
import net.opentsdb.contrib.tsquare.web.AnnotatedDataQuery;
import net.opentsdb.contrib.tsquare.web.DataQueryModel;
import net.opentsdb.contrib.tsquare.web.view.GraphiteJsonResponseWriter;
import net.opentsdb.contrib.tsquare.web.view.GraphiteRawResponseWriter;
import net.opentsdb.core.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * @author James Royalty (jroyalty) <i>[Jun 7, 2013]</i>
 */
@Controller
@RequestMapping("/graphite")
public class GraphiteController extends AbstractController {
    private static final Logger log = LoggerFactory.getLogger(GraphiteController.class);
    
    private static final Set<String> VALID_FORMATS = Sets.newHashSet("json", "raw");
    
    /**
     * Implements portions of the Graphite Render URL API.  See http://graphite.readthedocs.org/en/latest/render_api.html
     * 
     * @param target the name of the metric(s) to query.  Multiple target parameters may be
     * specified.  Graphite aggregation functions supported: averageSeries, sumSeries, maxSeries, minSeries, stddevSeries
     * @param from
     * @param until
     * @param format
     * @param jsonp
     * @param webRequest included to read extra Graphite URL parameters that aren't 
     * declared in the method signature
     * @param servletResponse 
     * @return
     */
    @RequestMapping(value = "/render", method=RequestMethod.GET)
    public ModelAndView render(
            @RequestParam(required=true) String[] target,
            @RequestParam(required=true) String from,
            @RequestParam(required=false) String until,
            @RequestParam(required=true) String format,
            @RequestParam(required=false) String noCache,
            final WebRequest webReques) throws IOException {
        
        // We only return JSON for integration with dashboard projects.
        Preconditions.checkArgument(VALID_FORMATS.contains(format.toLowerCase()), "Unsupported format: %s", format);
        
        final QueryDurationParams durationParams = parseDurations(from, until);
        if (log.isInfoEnabled()) {
            log.info("{}", durationParams);
        }
        
        // Prepare queries...
        final MetricParser parser = getTsdbManager().newMetricParser();
        final DataQueryModel model = new DataQueryModel();
        
        for (final String t : target) {
            final Query q = getTsdbManager().newMetricsQuery();
            durationParams.contributeToQuery(q);
            
            final Metric m = parser.parseMetric(t);
            m.contributeToQuery(q);
            
            model.addQuery(new AnnotatedDataQuery(m, q));
            
            log.info("Added {} to query", m);
        }
        
        if ("json".equalsIgnoreCase(format)) {
            model.setResponseWriter(new GraphiteJsonResponseWriter());
        } else if ("raw".equalsIgnoreCase(format)) {
            model.setResponseWriter(new GraphiteRawResponseWriter());
        } else {
            throw new IllegalStateException("Control should not reach this point without a valid output format.");
        }
        
        return model.toModelAndView();
    }
}
