#!/bin/sh
export LD_LIBRARY_PATH=.:$LD_LIBRARY_PATH
export CLASSPATH=../bin:$CLASSPATH
echo "Classpath is $CLASSPATH"
export CLASSNAME=uk.co.modularaudio.util.thread.GetThreadID
echo "Classname is $CLASSNAME"
export CLASS=`echo $CLASSNAME |sed "s:\.:/:g"`.java
echo "CLASS is $CLASS"
#echo "Compiling Java class..."
#javac ../src/$CLASS -d ../bin
echo "Creating JNI header file..."
javah -jni $CLASSNAME

echo "Building shared library..."
gcc -I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I/usr/include -o ../lib/libGetThreadID.so -shared -Wl,-soname,GetThreadID.so GetThreadID.c -fPIC

echo "libGetThreadID.so built"
