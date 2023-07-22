#include <jni.h>
#include <android/log.h>
#include <string>
#include "LameControl.h"

# define NELEM(x) ((int) (sizeof(x) / sizeof((x)[0])))
# define LAME_CONTROL_CLASS "com/hualee/lame/LameControl"


static JNINativeMethod lameControlNativeMethods[] = {
        /* name, signature, funcPtr */
        {"close",             "()V",        (void *) lame_control_close},
        {"encode",            "([S[SI[B)I", (void *) lame_control_encode},
        {"encodeInterleaved", "([SI[B)I",   (void *) lame_control_encode_interleaved},
        {"flush",             "([B)I",      (void *) lame_control_flush},
        {"init",              "(IIIII)V",   (void *) lame_control_init},
};



JNIEXPORT jint
JNI_OnLoad(JavaVM *vm, void * /* reserved */) {
    JNIEnv *env = nullptr;
    jint result = -1;

    if (vm->GetEnv((void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("ERROR: GetEnv failed\n");
        return result;
    }
    if (env == nullptr) {
        return result;
    }

    jclass lameClazz = env->FindClass(LAME_CONTROL_CLASS);
    if (lameClazz == nullptr) {
        LOGE("Can't find %s", LAME_CONTROL_CLASS);
        return result;
    }
    if (env->RegisterNatives(lameClazz, lameControlNativeMethods, NELEM(lameControlNativeMethods)) < 0) {
        LOGE("RegisterNatives failed");
        return result;
    }

    return JNI_VERSION_1_4;
}
