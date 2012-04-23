#!/bin/bash
# $Id$
# Simple script to run my hadoop log analyzer. All configuration 
# parameters are specified in the ## CONFIG ## block.

## CONFIG ##
HADOOP_HOME=/opt/hadoop-0.20.0
PROJECT_BASEDIR=/home/sujit/src/jtmt
INPUT_DIR=$PROJECT_BASEDIR/src/test/resources/phraseextractor/holding1
STOPWORD_FILE=$PROJECT_BASEDIR/src/main/resources/stopwords.txt
OUTPUT_DIR=$PROJECT_BASEDIR/src/test/resources/phraseextractor/outputs

cd $HADOOP_HOME
if [ -d $OUTPUT_DIR ]; then
  rm -rf $OUTPUT_DIR
fi
# copy over the word_break_rules.txt into a src/main/resources directory
# relative to the script root
bin/hadoop jar $PROJECT_BASEDIR/target/phrase-extractor.jar $INPUT_DIR $STOPWORD_FILE $OUTPUT_DIR
cd -
