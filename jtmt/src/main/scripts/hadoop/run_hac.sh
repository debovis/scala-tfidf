#!/bin/bash
# $Id$
# Script to call Term Document Matrix Generator

## CONFIG ##
M2_REPO=/home/sujit/.m2/repository
HADOOP_HOME=/opt/hadoop-0.20.0
PROJECT_BASEDIR=/home/sujit/src/jtmt
MODE=p # mode can be (l)ocal or (p)seudo-distributed
if [ $MODE == "l" ]; then
  PROTOCOL_PREFIX=$PROJECT_BASEDIR/src/test/resources
else
  PROTOCOL_PREFIX=hdfs://localhost:54310
fi
INPUT_DIR=/hac/input
TEMP_DIRS=/hac/temp*
## CONFIG ##

# for local mode
if [ $MODE == "l" ]; then
  export HADOOP_CLASSPATH=$CLASSPATH:\
$M2_REPO/commons-lang/commons-lang/2.1/commons-lang-2.1.jar:\
$M2_REPO/commons-math/commons-math/2.0/commons-math-2.0.jar
fi

cd $HADOOP_HOME
if [ $MODE == "l" ]; then
  rm -rf $PROTOCOL_PREFIX$TEMP_DIRS
  # no special packaging required for local mode
  bin/hadoop jar $PROJECT_BASEDIR/target/jtmt-1.0-SNAPSHOT.jar net.sf.jtmt.clustering.hadoop.agglomerative.HierarchicalAgglomerativeClusterer $PROTOCOL_PREFIX$INPUT_DIR
else
  bin/hadoop fs -rmr $TEMP_DIRS
  bin/hadoop jar $PROJECT_BASEDIR/target/ha-clusterer.jar $PROTOCOL_PREFIX$INPUT_DIR
fi
cd -
unset HADOOP_CLASSPATH

