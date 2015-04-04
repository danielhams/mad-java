#include <stdio.h>

#include <jni.h>

#include <sndfile.h>

#include <stdint.h>

#include <mpg123.h>

JNIEXPORT void Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_HandRolled(JNIEnv * env, jclass class, int length, jstring jstr)
{
    const char * cstr = (*env)->GetStringUTFChars(env,jstr,0);
    printf("HandRolled called with (%d) and (%s).\n", length, cstr);
    fflush(stdout);
    (*env)->ReleaseStringUTFChars(env,jstr,0);
}

JNIEXPORT jint Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_CheckFormat(JNIEnv * env, jclass class, mpg123_handle * mpg123Handle )
{
    long rate;
    int channels;
    int encoding;

    return mpg123_getformat( mpg123Handle, &rate, &channels, &encoding );
}

JNIEXPORT jlong Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_GetFormatSampleRate(JNIEnv * env, jclass class, mpg123_handle * mpg123Handle )
{
    long rate;
    int channels;
    int encoding;
    jlong retVal = 0;
    int getFormatSuccess = mpg123_getformat( mpg123Handle, &rate, &channels, &encoding );

    if( getFormatSuccess == MPG123_OK )
    {
        retVal = rate;
    }
    else
    {
        printf("GetFormatSampleRate indicated failure\n");
        fflush(stdout);
    }

    return retVal;
}

JNIEXPORT jint Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_GetFormatChannels(JNIEnv * env, jclass class, mpg123_handle * mpg123Handle )
{
    long rate;
    int channels;
    int encoding;
    jint retVal = 0;
    int getFormatSuccess = mpg123_getformat( mpg123Handle, &rate, &channels, &encoding );

    if( getFormatSuccess == MPG123_OK )
    {
        retVal = channels;
    }

    return retVal;
}

JNIEXPORT jint Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_DecodeData(JNIEnv * env, jclass class, mpg123_handle * mpg123Handle,
                                                                                     jfloatArray floatArray, jint outputFloatsOffset, jint numFloats,
                                                                                     int * done )
{
    int rv;
    size_t numBytesAvailable = numFloats * sizeof(float);
    size_t sDone;

    jfloat * arrayBody = (*env)->GetFloatArrayElements(env,floatArray,NULL);

    rv = mpg123_decode( mpg123Handle, NULL, 0,
                        (unsigned char *)&(arrayBody[outputFloatsOffset]),
                        numBytesAvailable,
                        &sDone );

    (*env)->ReleaseFloatArrayElements(env,floatArray,arrayBody,0);

    *done = sDone / sizeof(float);

    return rv;
}
