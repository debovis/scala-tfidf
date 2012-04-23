#!/bin/bash
# $Id$
# Simple script to run my hadoop log analyzer. All configuration 
# parameters are specified in the ## CONFIG ## block.

## CONFIG ##
HADOOP_HOME=/opt/hadoop-0.18.1
PROJECT_BASEDIR=/home/sujit/src/jtmt
INPUT_DIR=$PROJECT_BASEDIR/src/test/resources/access_logs
OUTPUT_DIR=$PROJECT_BASEDIR/src/test/resources/access_logs_outputs

cd $HADOOP_HOME
if [ -d $OUTPUT_DIR ]; then
  rm -rf $OUTPUT_DIR
fi
bin/hadoop jar $PROJECT_BASEDIR/target/query-coc-analyzer.jar $INPUT_DIR $OUTPUT_DIR
cd -
