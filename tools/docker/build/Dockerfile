FROM ubuntu:14.04

# Install basic dependencies
RUN apt-get install -y build-essential software-properties-common autoconf git python gnuplot maven

# Grab java from a well known public PPA
RUN add-apt-repository ppa:webupd8team/java
RUN apt-get update
RUN echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
RUN apt-get install -y oracle-java7-installer oracle-java7-set-default

# Build opentsdb and install its mvn artifacts.
# Note: This will take a while. OpenTSDB has quite a few tests.
RUN git clone git://github.com/OpenTSDB/opentsdb.git /usr/lib/opentsdb
RUN cd /usr/lib/opentsdb && git checkout v1.1.0
RUN cd /usr/lib/opentsdb && ./build.sh
RUN cd /usr/lib/opentsdb/build && make pom.xml
RUN cd /usr/lib/opentsdb/ && mvn -DskipTests=true -Dgpg.skip=true -Dmaven.repo.local=/root/.m2 clean install

# Build tsquare
RUN git clone https://github.com/Conductor/tsquare.git /usr/lib/tsquare
RUN cd /usr/lib/tsquare && mvn -Dmaven.repo.local=/root/.m2 clean package
CMD cat /usr/lib/tsquare/target/tsquare.war

