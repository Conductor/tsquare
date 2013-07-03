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

import java.lang.reflect.Field;

import net.opentsdb.contrib.tsquare.AggregatorFactory;
import net.opentsdb.contrib.tsquare.ExtendedTsdbMetricParser;
import net.opentsdb.contrib.tsquare.MetricParser;
import net.opentsdb.contrib.tsquare.TsdbManager;
import net.opentsdb.contrib.tsquare.UidQuery;
import net.opentsdb.uid.UniqueId;

import org.hbase.async.HBaseClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.ImmutableSet;

/**
 * @author James Royalty (jroyalty) <i>[Jun 7, 2013]</i>
 */
public class ProvidedTsdbManager implements TsdbManager, InitializingBean, DisposableBean {
//    private static final String METRICS_QUAL;
//    private static final short METRICS_WIDTH;
//    private static final String TAG_NAME_QUAL;
//    private static final short TAG_NAME_WIDTH;
//    private static final String TAG_VALUE_QUAL;
//    private static final short TAG_VALUE_WIDTH;
    
    private static final byte[] UID_ID_FAMILY;
    private static final byte[] UID_NAME_FAMILY;
    
    @Autowired
    private TSDB tsdb;
    @Autowired
    private HBaseClient hbaseClient;
    @Autowired
    private AggregatorFactory aggregatorFactory;
    
    private String tableName;
    private String uidTableName;
    
    private ImmutableSet<String> knownUidKinds;
    
    static {
        try {
            // This is nasty.  net.opentsdb.tools.UidManager does the same thing.
            // At least if any of these statics change we'll know because this
            // set-by-reflection stuff will break.
            Field f = null;
            
            /*
            f = ReflectionUtils.findField(TSDB.class, "METRICS_QUAL");
            ReflectionUtils.makeAccessible(f);
            METRICS_QUAL = (String) f.get(null);
            f = ReflectionUtils.findField(TSDB.class, "METRICS_WIDTH");
            ReflectionUtils.makeAccessible(f);
            METRICS_WIDTH = f.getShort(null);
            
            f = ReflectionUtils.findField(TSDB.class, "TAG_NAME_QUAL");
            ReflectionUtils.makeAccessible(f);
            TAG_NAME_QUAL = (String) f.get(null);
            f = ReflectionUtils.findField(TSDB.class, "TAG_NAME_WIDTH");
            ReflectionUtils.makeAccessible(f);
            TAG_NAME_WIDTH = f.getShort(null);
            
            f = ReflectionUtils.findField(TSDB.class, "TAG_VALUE_QUAL");
            ReflectionUtils.makeAccessible(f);
            TAG_VALUE_QUAL = (String) f.get(null);
            f = ReflectionUtils.findField(TSDB.class, "TAG_VALUE_WIDTH");
            ReflectionUtils.makeAccessible(f);
            TAG_VALUE_WIDTH = f.getShort(null);
            */
            
            f = ReflectionUtils.findField(UniqueId.class, "ID_FAMILY");
            ReflectionUtils.makeAccessible(f);
            UID_ID_FAMILY = (byte[]) f.get(null);
            f = ReflectionUtils.findField(UniqueId.class, "NAME_FAMILY");
            ReflectionUtils.makeAccessible(f);
            UID_NAME_FAMILY = (byte[]) f.get(null);
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
        return new TsdbUidQuery(tsdb, uidTableName, UID_ID_FAMILY);
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

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setUidTableName(String uidTableName) {
        this.uidTableName = uidTableName;
    }
}
