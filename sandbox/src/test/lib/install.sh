#!/bin/sh
mvn install:install-file -DgroupId=javax.resource -DartifactId=connector \
          -Dversion=1.0 -Dpackaging=jar -Dfile=connector-1.0.jar
