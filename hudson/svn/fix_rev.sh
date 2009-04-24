#!/bin/bash
# vim:ts=3:sw=3:expandtab:
set -e

# dump everything we do to a log
set -x
exec > >(tee -a /tmp/$0.log)
exec 2>&1

# Fix the svn:date of a revision in a SVN mirror
# http://subversion.tigris.org/issues/show_bug.cgi?id=3194

if [ $# != 2 ]; then
   echo 1>&2 "Usage: $0 <repos> <rev>"
   exit 1
fi

REPOS="$1"
REV="$2"

DATE=`svn pg -r $REV --revprop svn:date file://$REPOS`

if [ -n "$DATE" ]; then
   echo "$REV already has svn:date $DATE"
   exit 0
fi

PREV_REV=$REV
while [ -z "$DATE" ]; do
   PREV_REV=`expr $PREV_REV - 1`
   if [ $PREV_REV == -1 ]; then
      echo 1>&2 "can't find a previous date for $REV"
      exit 1
   fi
   DATE=`svn pg -r $PREV_REV --revprop svn:date file://$REPOS`
done

echo "setting svn:date to $DATE on $REV"
svn ps -r $REV --revprop svn:date $DATE file://$REPOS
