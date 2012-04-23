#!/bin/bash
# $Id$
# Script to call Term Document Matrix Generator

## CONFIG ##
M2_REPO=/home/sujit/.m2/repository
HADOOP_HOME=/opt/hadoop-0.20.0
PROJECT_BASEDIR=/home/sujit/src/jtmt
MODE=p # mode can be (l)ocal or (p)seudo-distributed
if [ $MODE == "l" ]; then
  PROTOCOL_PREFIX=""
  STOPWORD_FILE=$PROJECT_BASEDIR/src/main/resources/stopwords.txt
  INPUT_DIR=$PROJECT_BASEDIR/src/test/resources/hac/inputs
  OUTPUT_DIR=$PROJECT_BASEDIR/src/test/resources/hac/outputs
  TEMP_DIRS=$PROJECT_BASEDIR/src/test/resources/hac/temp*
else
  PROTOCOL_PREFIX=hdfs://localhost:54310
  STOPWORD_FILE=/user/sujit/tdmg/resources/stopwords.txt
  INPUT_DIR=/user/sujit/tdmg/inputs
  OUTPUT_DIR=/user/sujit/tdmg/outputs
  TEMP_DIRS=/user/sujit/tdmg/temp*
fi
## CONFIG ##

# for local mode
if [ $MODE == "l" ]; then
  export HADOOP_CLASSPATH=$CLASSPATH:\
$M2_REPO/org/apache/lucene/lucene-core/2.4.0/lucene-core-2.4.0.jar:\
$M2_REPO/org/apache/lucene-analyzers/2.3.0/lucene-analyzers-2.3.0.jar:\
$M2_REPO/commons-lang/commons-lang/2.1/commons-lang-2.1.jar:\
$PROJECT_BASEDIR/target/jtmt-1.0-SNAPSHOT.jar
fi

cd $HADOOP_HOME
if [ $MODE == "l" ]; then
  rm -rf $OUTPUT_DIR
  rm -rf $TEMP_DIRS
  # no special packaging required for local mode
  bin/hadoop jar $PROJECT_BASEDIR/target/jtmt-1.0-SNAPSHOT.jar net.sf.jtmt.indexers.hadoop.TermDocumentMatrixGenerator "" $STOPWORD_FILE $INPUT_DIR $OUTPUT_DIR
else
  bin/hadoop fs -rmr $OUTPUT_DIR
  bin/hadoop fs -rmr $TEMP_DIRS
  bin/hadoop jar $PROJECT_BASEDIR/target/tdm-generator.jar $PROTOCOL_PREFIX $STOPWORD_FILE $INPUT_DIR $OUTPUT_DIR
fi
cd -
unset HADOOP_CLASSPATH

