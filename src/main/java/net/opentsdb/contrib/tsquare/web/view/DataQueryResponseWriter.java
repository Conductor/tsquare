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

/**
 * @author James Royalty (jroyalty) <i>[Jul 29, 2013]</i>
 */
public interface DataQueryResponseWriter {
    void beginResponse(ResponseContext context) throws IOException;
    
    void endResponse(ResponseContext context) throws IOException;
    
    void onError(ResponseContext context, Throwable ex);
}
