package net.opentsdb.contrib.tsquare;

import org.hbase.async.HBaseException;

/**
 * @author James Royalty (jroyalty) <i>[Jun 18, 2013]</i>
 */
public interface UidQuery {
    void setRegex(String expression);
    
    void includeAllKinds();
    
    void includeKind(String kind);
    
    void run(QueryCallback<Uid> callback) throws HBaseException;
}
