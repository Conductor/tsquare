package net.opentsdb.contrib.tsquare.web.view;

import java.io.IOException;

import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;

/**
 * @author James Royalty (jroyalty) <i>[Aug 30, 2013]</i>
 */
public interface GroupedSeriesWriter extends DataQueryResponseWriter {
    void write(Iterable<AnnotatedDataPoints> groupedPoints, ResponseContext context) throws IOException;
}
