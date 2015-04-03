#include "stdio.h"

#include <jni.h>

#include <sndfile.h>

#include <stdint.h>

JNIEXPORT void Java_uk_co_modularaudio_libsndfilewrapper_swig_libsndfileJNI_HandRolled(JNIEnv * env, jclass class, int length, jstring jstr)
{
    const char * cstr = (*env)->GetStringUTFChars(env,jstr,0);
    printf("HandRolled called with (%d) and (%s).\n", length, cstr);
    fflush(stdout);
    (*env)->ReleaseStringUTFChars(env,jstr,0);
}

JNIEXPORT jlong JNICALL Java_uk_co_modularaudio_libsndfilewrapper_swig_libsndfileJNI_CustomSfReadFramesOffset(
    JNIEnv * env,
    jclass class, SNDFILE * sndfile, jint numChannels, jfloatArray floatArray, jint outputFramesOffset, jlong numFrames)
{
    jlong retVal;
    jsize arrayLength = (*env)->GetArrayLength(env,floatArray);

    int outputFloatOffset = outputFramesOffset*numChannels;

    if( (arrayLength - outputFloatOffset) < (numFrames*numChannels) )
    {
        return 0;
    }

    jfloat * arrayBody = (*env)->GetFloatArrayElements(env,floatArray,NULL);

    retVal = sf_readf_float( sndfile, &(arrayBody[outputFloatOffset]), numFrames );

    // Release and free any copy of the elements
    (*env)->ReleaseFloatArrayElements(env,floatArray,arrayBody,0);

    return retVal;
}
