
if [ -z "$JAVA_HOME" ]; then
	echo Please set your JAVA_HOME environment variable to proceed
	echo
	echo Fail.
	exit 1;
fi

echo Cleaning working files
rm -f libmpg123_wrap.c
rm -f ../lib/libmpg123_wrap.dylib
rm -f libmpg123_wrap.o
rm -f libmpg123_jnibulk.o
rm -rf ../src/uk/co/modularaudio/libmpg123wrapper/swig
echo done
echo

mkdir -p ../src/uk/co/modularaudio/libmpg123wrapper/swig
mkdir -p ../lib

echo Generating Shared Library Source File
swig -I/usr/include -I/Users/dan/ToolsInstall/include -java -package uk.co.modularaudio.libmpg123wrapper.swig -outdir ../src/uk/co/modularaudio/libmpg123wrapper/swig libmpg123.i
echo Generating Shared Library Wrapper Object
gcc -fpic -I/Users/dan/ToolsInstall/include -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -c libmpg123_wrap.c
echo Generating Custom Bulk Transfer Method Object
gcc -fpic -I/Users/dan/ToolsInstall/include -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -c libmpg123_jnibulk.c
echo Generating Shared Library Wrapper SO
gcc -shared -L/Users/dan/ToolsInstall/lib -L/usr/lib -lmpg123 -o ../lib/libmpg123_wrap.dylib libmpg123_wrap.o libmpg123_jnibulk.o
echo done
echo
