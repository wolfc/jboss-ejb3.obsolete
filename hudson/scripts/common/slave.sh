#!/bin/sh
wget -q -N http://mike.lab.bos.redhat.com:8380/hudson/jnlpJars/slave.jar -O slave.jar
exec /usr/java/jdk1.6.0_11/bin/java -jar slave.jar
