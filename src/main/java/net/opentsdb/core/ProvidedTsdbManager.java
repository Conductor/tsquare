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
package net.opentsdb.core;

import com.google.common.collect.ImmutableSet;

import org.hbase.async.HBaseClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import net.opentsdb.contrib.tsquare.AggregatorFactory;
import net.opentsdb.contrib.tsquare.ExtendedTsdbMetricParser;
import net.opentsdb.contrib.tsquare.MetricParser;
import net.opentsdb.contrib.tsquare.TsdbManager;
import net.opentsdb.contrib.tsquare.UidQuery;
import net.opentsdb.uid.UniqueId;

import java.lang.reflect.Field;

/**
 * @author James Royalty (jroyalty) <i>[Jun 7, 2013]</i>
 */
public class ProvidedTsdbManager implements TsdbManager, InitializingBean, DisposableBean {
    private static final byte[] UID_ID_FAMILY;
    
    @Autowired
    private TSDB tsdb;
    @Autowired
    private HBaseClient hbaseClient;
    @Autowired
    private AggregatorFactory aggregatorFactory;
    
    private ImmutableSet<String> knownUidKinds;
    
    static {
        try {
            // This is nasty.  net.opentsdb.tools.UidManager does the same thing.
            // At least if any of these statics change we'll know because this
            // set-by-reflection stuff will break.
            Field f = null;
            
            f = ReflectionUtils.findField(UniqueId.class, "ID_FAMILY");
            ReflectionUtils.makeAccessible(f);
            UID_ID_FAMILY = (byte[]) f.get(null);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @Override
    public AggregatorFactory getAggregatorFactory() {
        return aggregatorFactory;
    }

    @Override
    public MetricParser newMetricParser() {
        return new ExtendedTsdbMetricParser(aggregatorFactory, Aggregators.SUM);
    }

    @Override
    public Query newMetricsQuery() {
        return tsdb.newQuery();
    }
    
    @Override
    public UidQuery newUidQuery() {
        return new TsdbUidQuery(tsdb, UID_ID_FAMILY);
    }
    
    @Override
    public ImmutableSet<String> getKnownUidKinds() {
        return knownUidKinds;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        this.knownUidKinds = ImmutableSet.of(
                tsdb.metrics.kind(),
                tsdb.tag_names.kind(),
                tsdb.tag_values.kind());
    }

    @Override
    public void destroy() throws Exception {
        tsdb.shutdown().joinUninterruptibly();
    }

    public void setTsdb(TSDB tsdb) {
        this.tsdb = tsdb;
    }

    public void setHbaseClient(HBaseClient client) {
        this.hbaseClient = client;
    }

    public void setAggregatorFactory(AggregatorFactory aggregatorFactory) {
        this.aggregatorFactory = aggregatorFactory;
    }
}
