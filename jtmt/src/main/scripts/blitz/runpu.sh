#!/bin/bash
PROJECT_HOME=/home/sujit/src/jtmt
M2_REPO=$HOME/.m2/repository
BLITZ_HOME=/opt/blitz-2.1
CLASSPATH=\
$M2_REPO/com/sun/jini/jsk-lib/2.1/jsk-lib-2.1.jar:\
$M2_REPO/com/sun/jini/jsk-platform/2.1/jsk-platform-2.1.jar:\
$M2_REPO/commons-logging/commons-logging/1.0.4/commons-logging-1.0.4.jar:\
$M2_REPO/log4j/log4j/1.2.14/log4j-1.2.14.jar:\
$PROJECT_HOME/target/jtmt-1.0-SNAPSHOT.jar 

case "$1" in
  master) java -Djava.security.policy=$BLITZ_HOME/config/policy.all \
-classpath $CLASSPATH \
net.sf.jtmt.concurrent.blitz.Master 2>&1 | tee $1.log
     ;;
  slave) java -Djava.security.policy=$BLITZ_HOME/config/policy.all \
-classpath $CLASSPATH \
net.sf.jtmt.concurrent.blitz.Slave 2>&1 | tee $1.log
     ;;
  *) echo "Usage: $0 {master|slave}"
     ;;
esac
