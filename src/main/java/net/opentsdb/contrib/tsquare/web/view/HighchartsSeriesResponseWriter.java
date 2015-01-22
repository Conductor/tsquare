package net.opentsdb.contrib.tsquare.web.view;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import net.opentsdb.contrib.tsquare.web.AnnotatedDataPoints;
import net.opentsdb.core.DataPoint;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 * @author James Royalty (jroyalty) <i>[Jan 19, 2014]</i>
 */
public class HighchartsSeriesResponseWriter extends AbstractJsonResponseWriter implements SingleSeriesWriter {
    public HighchartsSeriesResponseWriter(final boolean millisecondResolution) {
        super(millisecondResolution);
    }
    
    @Override
    public void beginResponse(ResponseContext context) throws IOException {
        super.beginResponse(context);
        
        // START of response array 
        getJsonGenerator(context).writeStartArray(); 
    }
    
    @Override
    public void write(final AnnotatedDataPoints annotatedPoints, final ResponseContext context) throws IOException {
        final JsonGenerator jsonGenerator = getJsonGenerator(context);
        
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("name", annotatedPoints.getDataPoints().metricName());
        
        jsonGenerator.writeArrayFieldStart("data");

        for (final DataPoint p : annotatedPoints.getDataPoints()) {
            jsonGenerator.writeStartArray();
            
            if (isMillisecondResolution()) {
                jsonGenerator.writeNumber(p.timestamp());
            } else {
                jsonGenerator.writeNumber(TimeUnit.MILLISECONDS.toSeconds(p.timestamp()));
            }

            if (p.isInteger()) {
                jsonGenerator.writeNumber(p.longValue());
            } else {
                jsonGenerator.writeNumber(p.doubleValue());
            }

            jsonGenerator.writeEndArray();
        }

        // end of "data" array
        jsonGenerator.writeEndArray();
 
        // end of single series object
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
