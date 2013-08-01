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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Maps;

/**
 * @author James Royalty (jroyalty) <i>[Jul 19, 2013]</i>
 */
public final class ResponseContext {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Map<String, Object> properties;
    
    public ResponseContext(final HttpServletRequest request, final HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.properties = Maps.newLinkedHashMap();
    }
    
    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public Object putProperty(final String name, final Object value) {
        return properties.put(name, value);
    }
    
    public <T> T getProperty(final String name, final Class<T> clazz) {
        final Object value = properties.get(name);
        if (value == null) {
            return null;
        } else {
            return clazz.cast(value);
        }
    }
    
    public <T> T getProperty(final String name, final T defaultValue, final Class<T> clazz) {
        final Object value = properties.get(name);
        if (value == null) {
            return defaultValue;
        } else {
            return clazz.cast(value);
        }
    }
}
