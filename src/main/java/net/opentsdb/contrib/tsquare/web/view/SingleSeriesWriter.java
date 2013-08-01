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

import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;

/**
 * Writes response data a <strong>single series</strong> at a time.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 29, 2013]</i>
 */
public interface SingleSeriesWriter extends DataQueryResponseWriter {
    /**
     * Write data for the given series.  When a query returns multiple series
     * then this method will be called once for each series.
     * 
     * @param points
     * @param context
     * @throws IOException
     */
    void write(AnnotatedDataPoints annotatedPoints, ResponseContext context) throws IOException;
}
