if [ -z "$JAVA_HOME" ]; then
	echo Please set your JAVA_HOME environment variable to proceed
	echo
	echo Fail.
	exit 1;
fi

export LCF=`pkg-config --cflags sndfile`
export LLF=`pkg-config --libs sndfile`

export SWIGDIR=swig
export LIBDIR=lib
export JAVAOUTDIR=src/uk/co/modularaudio/libsndfilewrapper/swig
export PACKAGE=uk.co.modularaudio.libsndfilewrapper.swig

echo Cleaning working files
rm -f ${SWIGDIR}/libsndfile_wrap.c
rm -f ${LIBDIR}/libsndfile_wrap.dylib
rm -f ${LIBDIR}/libsndfile_wrap.o
rm -f ${LIBDIR}/libsndfile_jnibulk.o
rm -rf ${JAVAOUTDIR}
echo done
echo

mkdir -p ${JAVAOUTDIR}
mkdir -p ${LIBDIR}

echo Generating Shared Library Source File
swig ${LCF} -I/usr/include -java -package ${PACKAGE} -outdir ${JAVAOUTDIR} ${SWIGDIR}/libsndfile.i
echo Generating Shared Library Wrapper object
gcc -fpic ${LCF} -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -c ${SWIGDIR}/libsndfile_wrap.c -o ${LIBDIR}/libsndfile_wrap.o
echo Generating Custom Bulk Transfer Method Object
gcc -fpic ${LCF} -I$JAVA_HOME/include -I$JAVA_HOME/include/darwin -c ${SWIGDIR}/libsndfile_jnibulk.c -o ${LIBDIR}/libsndfile_jnibulk.o
echo Generating Shared Library Wrapper SO
gcc -shared -L/usr/lib ${LLF} -o ${LIBDIR}/libsndfile_wrap.dylib ${LIBDIR}/libsndfile_wrap.o ${LIBDIR}/libsndfile_jnibulk.o
echo done
echo
