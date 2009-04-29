#!/bin/sh
set -e
#set -x

DIRNAME=`dirname $0`
HERE=${DIRNAME:-.}

if [ $# -lt 1 ]; then
   echo 1>&2 "Usage: $0 <path>"
   exit 1
fi

REPO_PATH=$1
URL=file://$REPO_PATH

PREV_REV=`svn info $URL | grep "^Revision:" | cut -c11-`
PREV_REV=`expr $PREV_REV + 1`
/usr/bin/svnsync synchronize $URL
CURRENT_REV=`svn info $URL | grep "^Revision:" | cut -c11-`

for rev in $(seq $PREV_REV $CURRENT_REV); do
   $HERE/fix_rev.sh $REPO_PATH $rev
done
