sudo easy_install xlrd
dir=`pwd`
( cd $dir/temp/WordNet-3.0; make distclean)
rm -rf $dir/temp
mkdir $dir/temp
curl http://wordnetcode.princeton.edu/3.0/WordNet-3.0.tar.gz > $dir/temp/wordnet.tar.gz
tar -zxvf $dir/temp/wordnet.tar.gz 
mv ./WordNet-3.0/ $dir/temp
( cd $dir/temp/WordNet-3.0; ./configure --prefix=/usr/local)
( cd $dir/temp/WordNet-3.0; make)
( cd $dir/temp/WordNet-3.0; make install)