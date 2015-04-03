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

%native (CustomSfReadFramesOffset) jlong CustomSfReadFramesOffset( SNDFILE sndfile, jint numChannels, jfloatArray floatArray, jint outputFramesOffset, jlong numFrames);
%{
    JNIEXPORT jlong JNICALL Java_uk_co_modularaudio_libsndfilewrapper_swig_libsndfileJNI_CustomSfReadFramesOffset(JNIEnv *, jclass, SNDFILE *, jint, jfloatArray, jint, jlong );
%}
