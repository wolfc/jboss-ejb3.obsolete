#!/bin/bash

if [ $# != 1 ]; then
   echo 1>&2 "Usage: $0 <tests>"
   exit 1
fi
TESTS=$1

if [ -z "$JAVA_HOME" ]; then
   echo "JAVA_HOME is not set (no JDK selected)"
   exit 1
fi

set -x

#wget -N http://mike.lab.bos.redhat.com:8380/hudson/job/JBoss-AS-5.x-plugged/lastSuccessfulBuild/artifact/jboss/jboss-5.x-plugged.zip
#wget -nv -N http://mike.lab.bos.redhat.com:8380/hudson/job/JBoss-AS-5.x-latest/lastSuccessfulBuild/artifact/Branch_5_x/build/output/jboss-5.x-latest.zip
wget -nv -N http://mike.lab.bos.redhat.com:8380/hudson/job/EAP-5_x_latest_EJB3/lastSuccessfulBuild/artifact/JBPAPP_5_0/build/output/eap-5.x-latest-ejb3.zip
#if [ eap-5.x-latest-ejb3.zip -nt jboss ]; then
   rm -rf jboss
   unzip -q -d jboss eap-5.x-latest-ejb3.zip
   touch jboss
#fi

# Nuke any previous results so they won't interfere for sure
rm -rf javaeetck/bin/JTreport
rm -rf javaeetck/bin/JTwork

wget -nv -N http://mike.lab.bos.redhat.com:8380/hudson/job/tck51_package/lastSuccessfulBuild/artifact/javaeetck.zip
if [ javaeetck.zip -nt javaeetck ]; then
   rm -rf javaeetck
   unzip javaeetck.zip
   touch javaeetck
fi

wget -nv -N http://mike.lab.bos.redhat.com:8380/hudson/job/glassfish-package/lastSuccessfulBuild/artifact/glassfish.zip
if [ glassfish.zip -nt glassfish ]; then
   rm -rf glassfish
   unzip glassfish.zip
   touch glassfish
fi

export JAVAEE_HOME=${WORKSPACE}/glassfish
export JBOSS_HOME=`echo ${WORKSPACE}/jboss/*`
export TS_HOME=`echo ${WORKSPACE}/javaeetck`

cd javaeetck/j2eetck-mods
/opt/apache/ant/apache-ant-1.7.1/bin/ant

cd ${WORKSPACE}/javaeetck/bin
./tsant config.vi



cd $TS_HOME/bin
./tsant -f xml/s1as.xml start.javadb
./tsant init.javadb

(cd $JBOSS_HOME/bin; ./run.sh -c cts -b localhost) &
PID=$!

trap "${JBOSS_HOME}/bin/shutdown.sh -S; ./stop-javadb; sleep 15; /sbin/fuser -k $JBOSS_HOME/bin/run.jar" EXIT

~/common/waitfor $JBOSS_HOME/server/cts/log/server.log "Started in" 180

set -x
./tsant "-Dmultiple.tests=$TESTS" runclient

$JAVA_HOME/bin/java -cp ../lib/javatest.jar:../lib/tsharness.jar:../lib/cts.jar com.sun.javatest.cof.Main -o JTreport/report.xml JTwork

#kill $PID
#./stop-javadb
