/**
 * Base Giraffe (http://kenhub.github.io/giraffe/) dashboard config
 * for displaying metrics from TSquare's Graphite endpoint.
 *
 * Author: James Royalty
 */


// We point this to TSquare's Graphite endpoint...
var graphite_url = "http://localhost:8080/tsquare/graphite";

var dashboards = [
    {
	"name": "Example Dashboard",
	"refresh": 60000,
	"metrics": [
	    {
		"alias": "Some single metric",
		// We must always wrap the call to m() in a function in order to 
		// get access to the 'period' value.
		"target": function() { return m("my.metric.name", "avg", "avg"); }
	    },
	    {
		"alias": "Some array of metrics",
		// We can include multiple invocation to m().
		"target": [
		    function() { return m("my.metric.1", "avg", "avg"); },
		    function() { return m("my.metric.2", "avg", "sum"); },
		    function() { return m("my.metric.3", "sum", "sum"); }
		]
	    }
	]
    }
];

/**
 * Uses the 'period' global variable to determine an appropriate downsampling
 * interval to use with the given metric.  These will likely changed based
 * on your needs.  We've found these to be helpful.
 *
 * @param metric_name the name of the metric
 * @param downsampler_name name of the downsampler function
 * @param aggregator_name name of the aggragation function
 *
 * @return a properly formatted metric name aggregation and downsampling set
 */
function m( metric_name, downsampler_name, aggregator_name ) {
    var downsample_interval;

    if (typeof period == 'undefined') {
	downsample_interval = '1m';
    }
    else if (period <= 60) { // less than 1 hour
	downsample_interval = '1m';
    }
    else if (period <= (3*60)) { // less than 3 hours
	downsample_interval = '15m';
    }
    else if (period <= (24*60)) { // less than 1 day
	downsample_interval = '30m';
    }
    else if (period <= (7*24*60)) { // less than 1 week
	downsample_interval = '1d';
    }

    if (downsample_interval == null) {
	return aggregator_name + ':' + metric_name;
    }
    else {
	return aggregator_name 
	    + ':' + downsample_interval + '-' + downsampler_name
	    + ':' + metric_name;
    }
}
