
if [ -z "$JAVA_HOME" ]; then
	echo Please set your JAVA_HOME environment variable to proceed
	echo
	echo Fail.
	exit 1;
fi

echo Cleaning working files
rm -f libsndfile_wrap.c
rm -f ../lib/libsndfile_wrap.dylib
rm -f libsndfile_wrap.o
rm -f libsndfile_jnibulk.o
rm -rf ../src/uk/co/modularaudio/libsndfilewrapper/swig
echo done
echo

mkdir -p ../src/uk/co/modularaudio/libsndfilewrapper/swig
mkdir -p ../lib

echo Generating Shared Library Source File
swig -I/usr/include -I/Users/dan/ToolsInstall/include -java -package uk.co.modularaudio.libsndfilewrapper.swig -outdir ../src/uk/co/modularaudio/libsndfilewrapper/swig libsndfile.i
echo Generating Shared Library Wrapper Object
gcc -fpic -I/Users/dan/ToolsInstall/include -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -c libsndfile_wrap.c
echo Generating Custom Bulk Transfer Method Object
gcc -fpic -I/Users/dan/ToolsInstall/include -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -c libsndfile_jnibulk.c
echo Generating Shared Library Wrapper SO
gcc -shared -L/Users/dan/ToolsInstall/lib -L/usr/lib -lsndfile -o ../lib/libsndfile_wrap.dylib libsndfile_wrap.o libsndfile_jnibulk.o
echo done
echo
