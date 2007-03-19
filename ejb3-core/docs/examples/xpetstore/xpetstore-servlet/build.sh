#!/bin/sh
#==================================================
# Ant script
#
# Author:   Herve Tchepannou
# Version:  $Revision: 1.1 $
#==================================================

#--------------------------------------------------
# Resolve the jdk home directory
#--------------------------------------------------
if [ ! -z "$JAVA_PATH" -a -z "$JDKHOME" ] ; then
    JDKHOME=$JAVA_PATH
fi

if [ ! -z "$JDK_HOME" -a -z "$JDKHOME" ] ; then
    JDKHOME=$JDK_HOME
fi

if [ -z "$JDKHOME" ] ; then
    echo "Cannot find JDK. Please set the JDK_HOME environment variable to point"
    echo "to your JDK installation directory"
    echo ""
    exit 1
fi


#--------------------------------------------------
# build classpath
#--------------------------------------------------
LIBPATH=../lib/ant

LCP=$JDKHOME/lib/tools.jar
for dir in `ls -d $LIBPATH/*`
do
	for file in `ls $dir/*`
	do
		LCP=$file:$LCP
	done
done
echo CLASSPATH=$LCP


#--------------------------------------------------
# run ant
#--------------------------------------------------
$JDKHOME/bin/java -cp $LCP org.apache.tools.ant.Main $@

