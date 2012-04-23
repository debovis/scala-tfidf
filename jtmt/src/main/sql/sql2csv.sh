#!/bin/bash
# $Id$
# $Source$
# Code stolen from http://tlug.dnho.net/?q=node/209
MYSQL_USER=root
MYSQL_PASS=orange
MYSQL_DB=tmdb
MYSQL_SQL="select * from my_colloc"
OUTFILE=/tmp/my_colloc.csv
echo "mysql --user=$MYSQL_USER --password=$MYSQL_PASS $MYSQL_DB --execute=\"$MYSQL_SQL;\""
mysql --user=$MYSQL_USER --password=$MYSQL_PASS $MYSQL_DB --execute="$MYSQL_SQL;" | sed -e 's/\t/","/g;s/^/"/;s/$/"/;s/\n//g' >$OUTFILE
