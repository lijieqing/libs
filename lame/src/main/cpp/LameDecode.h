//
// Created by jieli51 on 2023/3/3.
//
#include <jni.h>
#include "android/log.h"
#include "IFlytek.h"

#ifdef __cplusplus
extern "C" {
#endif

void lame_hip_decode_init(JNIEnv *env, jclass clazz);

void lame_hip_decode(JNIEnv *env, jclass clazz, jbyteArray mp3buf_, int mp3buf_len, jobject bundle);

void lame_hip_decode_close(JNIEnv *env, jclass clazz);

jint lame_mp3_to_pcm(JNIEnv *env, jclass, jstring, jstring);

#ifdef __cplusplus
}
#endif

