#include <stdio.h>

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

JNIEXPORT jlong JNICALL Java_uk_co_modularaudio_libsndfilewrapper_swig_libsndfileJNI_CustomSfReadFloatsOffset(
    JNIEnv * env,
    jclass class, SNDFILE * sndfile, jfloatArray floatArray, jint outputFloatsOffset, jlong numToRead)
{
    jlong retVal;
    jsize arrayLength = (*env)->GetArrayLength(env,floatArray);

    if( (arrayLength - outputFloatsOffset) < numToRead )
    {
        return 0;
    }

    jfloat * arrayBody = (*env)->GetFloatArrayElements(env,floatArray,NULL);

    retVal = sf_read_float( sndfile, &(arrayBody[outputFloatsOffset]), numToRead );

    // Release and free any copy of the elements
    (*env)->ReleaseFloatArrayElements(env,floatArray,arrayBody,0);

    return retVal;
}
