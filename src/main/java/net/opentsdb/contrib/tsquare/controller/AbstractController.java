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

import net.opentsdb.contrib.tsquare.DateTimeExpressionParser;
import net.opentsdb.contrib.tsquare.TsdbManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

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
