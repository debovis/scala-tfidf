The code here does not work within the project. Its here so I can store
this stuff somewhere. To make these plugins work, copy them under a 
working nutch installation, like so:

$THIS_DIR=`pwd`
cd $NUTCH_HOME/src/plugin
mkdir myplugins
cp $THIS_DIR/plugins/*xml $NUTCH_HOME/src/plugin/myplugins
mkdir -p myplugins/src/java/net/sf/jtmt/indexing/nutch/plugins/parsing
cp $THIS_DIR/plugins/parsing/*java myplugins/src/java/net/sf/jtmt/indexing/nutch/plugins/parsing
mkdir -p myplugins/src/java/net/sf/jtmt/indexing/nutch/plugins/indexing
cp $THIS_DIR/plugins/indexing/*java myplugins/src/java/net/sf/jtmt/indexing/nutch/plugins/indexing

Then run ant in the myplugins directory, then on its parent directory. The
myplugins-jar and plugin.xml should be in the build directory of the parent,
which should then be copied to $NUTCH_HOME/plugin/myplugins.

To copy the indexer2 code, do this:
cd $NUTCH_HOME/src/java
mkdir -p net/sf/jtmt/indexing/nutch/indexer2
cp $THIS_DIR/indexer2/*java net/sf/jtmt/indexing/nutch/indexer2

Then run ant from the nutch root directory, this should build a nutch-1.0.jar,
which should be copied to the lib directory and the indexer2 class called
using bin/nutch with the full class name and parameters.

