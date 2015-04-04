#include <stdio.h>

#include <jni.h>

#include <sndfile.h>

#include <stdint.h>

JNIEXPORT void Java_uk_co_modularaudio_libmpg123wrapper_swig_libmpg123JNI_HandRolled(JNIEnv * env, jclass class, int length, jstring jstr)
{
    const char * cstr = (*env)->GetStringUTFChars(env,jstr,0);
    printf("HandRolled called with (%d) and (%s).\n", length, cstr);
    fflush(stdout);
    (*env)->ReleaseStringUTFChars(env,jstr,0);
}
