TSquare
=======

TSquare is the *Time Series Query, Reporting and Exploration* tool.  It offers
an extended HTTP-based API for use with [OpenTSDB v1.x](http://opentsdb.net/).
In particular, TSquare includes (currently) limited support for Graphite's
[render API](http://graphite.readthedocs.org/en/latest/render_api.html).  Other
features include:

* Extended JSON query response format, including support for JSONP responses
* A **grep** endpoint that allows you to search over metric names and tags
  using regular expressions and/or wildcards
* Aggregator factory that allows lookup of aggregators based on metric name
  (e.g by regex and/or wildcard)
* Extended metric query format that allows for default and auto-assigned
  aggregators (used in concert with the aggregator factory)
* More aggregators (last value, N-th percentile)

N.B. We wrote TSquare before OpenTSDB v2.x was available.  OpenTSDB v2 has an
extended REST API itself, so you might notice some overlap in features.  It's
very likely that we will port the *unique* features of TSquare over to OpenTSDB
v2 soon.


Building TSquare
----------------

To build TSquare you'll need:

* Java 1.6+ and
* Maven 3.x 
* OpenTSDB v1.x (see below)

Because OpenTSDB v1.x is not in any public Maven repository you'll need to
download and build it yourself.  Luckily, it's very easy.  Just follow the
building instructions on their [getting started](http://opentsdb.net/getting-started.html) page.

At this point, you should have successfully built OpenTSDB.  The next step is
to build it with Maven and install the artifact in your local Maven repository.
Here's how:

    ## from within the OpenTSDB base directory (the one with build.sh in it)
    cd build
    make pom.xml
    cd ..
    mvn clean install

Now you are ready to build TSquare, which builds as a .war file:

    ## from with the TSquare base directory
    mvn clean package

The resulting artifact is:  **target/tsquare.war**

**Building using Docker**

We've included a Dockerfile that makes building TSqure really easy.  Assuming you already have [Docker](https://www.docker.com/) installed and running you can generate the WAR with two commands:

    docker build -t="tsquare:v1" .
    docker run -i -t tsquare:v1 > tsquare.war


Running TSquare
---------------

To run TSquare you'll need servlet container like
[Tomcat](http://tomcat.apache.org/) or [Jetty](http://www.eclipse.org/jetty/).
(We've only tested TSquare with Tomcat.)  Just deploy it as per the
requirements of your specific container.


Authors
-------

The initial version of this application was written by James Royalty.

All contributors, sorted alphabetically:

* Daniel Aquino (https://github.com/chino)
* James Royalty (http://hackoeur.com/)


License
-------

Copyright (C) 2013 Conductor, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

