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
package net.opentsdb.contrib.tsquare.support;

import java.util.List;
import java.util.Map;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A Spring {@link PropertySource} that reads data from a parsed JSON document.
 * 
 * @author James Royalty (jroyalty) <i>[Jun 17, 2013]</i>
 */
public class JsonPropertySource extends PropertySource<JsonNode> {
    public JsonPropertySource(final String name, final JsonNode root) {
        super(name, root);
    }
    
    @Override
    public Object getProperty(final String name) throws ConversionNotSupportedException {
        String[] path = StringUtils.delimitedListToStringArray(name, ".");
        
        JsonNode contextNode = getSource();
        for (final String step : path) {
            contextNode = contextNode.get(step);
            
            if (contextNode == null || contextNode.isNull() || contextNode.isMissingNode()) {
                return null;
            }
        }
        
        if (contextNode.isFloatingPointNumber()) {
            return contextNode.asDouble();  // Promotion
        } else if (contextNode.isIntegralNumber()) {
            return contextNode.asLong();    // Promotion
        } else if (contextNode.isTextual()) {
            return contextNode.asText();
        } else if (contextNode.isBoolean()) {
            return contextNode.asBoolean();
        } else if (contextNode.isContainerNode()) {
            final ObjectMapper mapper = TsWebUtils.newObjectMapper();
            
            try {
                if (contextNode.isArray()) {
                    return mapper.treeToValue(contextNode, List.class);
                } else {
                    return mapper.treeToValue(contextNode, Map.class);
                }
            } catch (JsonProcessingException e) {
                if (contextNode.isArray()) {
                    throw new ConversionNotSupportedException(contextNode, List.class, e);
                } else {
                    throw new ConversionNotSupportedException(contextNode, Map.class, e);
                }
            }
        } else {
            throw new IllegalStateException("Unsupported JSON type in: " + contextNode);
        }
    }
}
