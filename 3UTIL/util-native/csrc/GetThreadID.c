#include <jni.h>
#include <unistd.h>
#include <sys/syscall.h>
#include "uk_co_modularaudio_util_thread_GetThreadID.h"

JNIEXPORT jint JNICALL
Java_uk_co_modularaudio_util_thread_GetThreadID_get_1tid(JNIEnv *env, jobject obj)
{
    jint tid = syscall(__NR_gettid);
    return tid;
}
