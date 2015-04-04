%module libsndfile

/* simplifies how arrays are handled - see 21.8.3 'Wrapping C arrays with Java arrays' in the SWIG docs */

%include "carrays.i"
%array_class(float, CArrayFloat);
%array_class(double, CArrayDouble);
%array_class(int, CArrayInt);
%array_class(short, CArrayShort);

/* converts the sf_count_t (__int64 typedef) to a long */
%include "stdint.i"

%{
#include "sndfile.h"
%}

%include "sndfile.h"

/*
 * A method for bulk reading into a java float array so
 * we're not going item by item.
 */
%native (HandRolled) void HandRolled(int, jstring jstr);
%{
    JNIEXPORT void JNICALL Java_uk_co_modularaudio_libsndfilewrapper_swig_libsndfileJNI_HandRolled(JNIEnv *, jclass, jint, jstring);
%}

%native (CustomSfReadFloatsOffset) jlong CustomSfReadFloatsOffset( SNDFILE sndfile, jfloatArray floatArray, jint outputFloatsOffset, jlong numFloats);
%{
    JNIEXPORT jlong JNICALL Java_uk_co_modularaudio_libsndfilewrapper_swig_libsndfileJNI_CustomSfReadFloatsOffset(JNIEnv *, jclass, SNDFILE *, jfloatArray, jint, jlong );
%}
