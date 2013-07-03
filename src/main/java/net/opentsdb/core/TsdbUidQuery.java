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

import java.util.ArrayList;

import net.opentsdb.contrib.tsquare.QueryCallback;
import net.opentsdb.contrib.tsquare.Uid;
import net.opentsdb.contrib.tsquare.UidQuery;
import net.opentsdb.contrib.tsquare.support.TsWebUtils;

import org.hbase.async.Bytes;
import org.hbase.async.HBaseException;
import org.hbase.async.KeyValue;
import org.hbase.async.Scanner;

import com.google.common.base.Strings;

/**
 * @author James Royalty (jroyalty) <i>[Jun 21, 2013]</i>
 */
final class TsdbUidQuery implements UidQuery {
    private String regex;
    private String includeKind;
    private int maxNumRows = 1024;
    
    private final TSDB tsdb;
    private final String tableName;
    private final byte[] columnFamily;
    
    TsdbUidQuery(final TSDB tsdb, final String tableName, final byte[] columnFamily) {
        this.tsdb = tsdb;
        this.tableName = tableName;
        this.columnFamily = columnFamily;
        
        // Initially, we include only metrics.
        includeKind = tsdb.metrics.kind();
    }
    
    @Override
    public void run(final QueryCallback<Uid> callback) throws HBaseException {
        final Scanner scanner = tsdb.client.newScanner(tableName);
        scanner.setFamily(columnFamily);
        scanner.setKeyRegexp(regex, TsWebUtils.CHARSET);
        
        if (!Strings.isNullOrEmpty(includeKind)) {
            scanner.setQualifier(includeKind);
        }
        
        if (maxNumRows > 0) {
            scanner.setMaxNumRows(maxNumRows);
        }
        
        try {
            ArrayList<ArrayList<KeyValue>> rows;

            while ( (rows = scanner.nextRows().joinUninterruptibly()) != null) {
                for (final ArrayList<KeyValue> row : rows) {
                    final String name = TsWebUtils.toString(row.get(0).key());
                    
                    for (final KeyValue kv : row) {
                        if (Bytes.equals(kv.family(), columnFamily)) {
                            final String kind = TsWebUtils.toString(kv.qualifier());
                            Uid uid = new Uid(name, kind);
                            boolean keepScanning = callback.onResult(uid);
                            if (!keepScanning) {
                                throw new QueryAbortedException();
                            }
                        }
                    }
                }
            }
        } catch (QueryAbortedException e) {
            return;
        } catch (HBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("UID query callback threw an exception.", e);
        } finally {
            scanner.close();
        }
    }
    
    @Override
    public void setRegex(String expression) {
        regex = expression;
    }
    
    @Override
    public void includeAllKinds() {
        this.includeKind = null;
    }
    
    @Override
    public void includeKind(String kind) {
        this.includeKind = kind;
    }
}
