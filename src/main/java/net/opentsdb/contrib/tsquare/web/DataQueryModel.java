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
package net.opentsdb.contrib.tsquare.web;

import java.util.Map;
import java.util.Set;

import net.opentsdb.contrib.tsquare.web.view.DataQueryResponseWriter;
import net.opentsdb.contrib.tsquare.web.view.DataQueryView;
import net.opentsdb.core.Query;

import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.Sets;

/**
 * Model object for sending {@link Query}s to the view.  This model object
 * is tightly bound to {@link DataQueryView}.
 * 
 * @author James Royalty (jroyalty) <i>[Jul 31, 2013]</i>
 */
public final class DataQueryModel {
    private Set<AnnotatedDataQuery> queries;
    private DataQueryResponseWriter responseWriter;
    
    public DataQueryModel() {
        this.queries = Sets.newLinkedHashSet();
    }
    
    public DataQueryModel(final DataQueryResponseWriter responseWriter) {
        this.queries = Sets.newLinkedHashSet();
        this.responseWriter = responseWriter;
    }
    
    public DataQueryModel(final Map<String, ?> modelMap) {
        if (modelMap == null || !modelMap.containsKey("DataQueryModel")) {
            throw new IllegalArgumentException("Model map does not contain a 'DataQueryModel' element");
        }
        
        final DataQueryModel m = (DataQueryModel) modelMap.get("DataQueryModel");
        this.queries = m.getQueries();
        this.responseWriter = m.getResponseWriter();
    }
    
    public void addQuery(final AnnotatedDataQuery query) {
        queries.add(query);
    }
    
    public Set<AnnotatedDataQuery> getQueries() {
        return queries;
    }
    
    public DataQueryResponseWriter getResponseWriter() {
        return responseWriter;
    }

    public void setResponseWriter(DataQueryResponseWriter writer) {
        this.responseWriter = writer;
    }

    public ModelAndView toModelAndView() {
        return new ModelAndView(DataQueryView.class.getSimpleName(), "DataQueryModel", this);
    }
}
