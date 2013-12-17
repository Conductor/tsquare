package net.opentsdb.contrib.tsquare.web.view;

import java.io.IOException;

import net.opentsdb.contrib.tsquare.support.DataPointsAsDoubles;
import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;
import net.opentsdb.core.Aggregator;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author James Royalty (jroyalty) <i>[Aug 30, 2013]</i>
 */
public class SummarizedJsonResponseWriter extends AbstractJsonResponseWriter implements GroupedSeriesWriter {
    @Override
    public void beginResponse(ResponseContext context) throws IOException {
        super.beginResponse(context);
        
        // START of response array 
        getJsonGenerator(context).writeStartArray(); 
    }

    @Override
    public void write(final Iterable<AnnotatedDataPoints> groupedPoints, final ResponseContext context) throws IOException {
        String metricName = null;
        Aggregator aggregator = null;
        StorelessUnivariateStatistic summarizer = null;
        
        for (final AnnotatedDataPoints dps : groupedPoints) {
            if (metricName == null) {
                // All points in 'groupedPoints' come from the same series.
                metricName = dps.getDataPoints().metricName();
            }
            
            if (aggregator == null) {
                aggregator = dps.getMetric().getAggregator();
                final String aggClassName = aggregator.getClass().getSimpleName();
                // HACK: We need to select summarizers in a way that's similar to Aggregators.
                if ("avg".equalsIgnoreCase(aggClassName)) {
                    summarizer = new Mean();
                } else {
                    summarizer = new Sum();
                }
            }
            
            final DataPointsAsDoubles doubles = new DataPointsAsDoubles(dps.getDataPoints(), dps.getQueryRangeInSeconds());
            final double aggValue = aggregator.runDouble(doubles);
            summarizer.increment(aggValue);
        }
        
        final JsonGenerator jsonGenerator = getJsonGenerator(context);
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("target", metricName);
        jsonGenerator.writeNumberField("summarizedValue", summarizer.getResult());
        jsonGenerator.writeEndObject();
    }
    
    @Override
    public void endResponse(final ResponseContext context) throws IOException {
        // END of response array
        getJsonGenerator(context).writeEndArray();
        
        // END of super class response (which might include JSONP wrapper).
        super.endResponse(context);
        
        getJsonGenerator(context).flush();
    }
}
