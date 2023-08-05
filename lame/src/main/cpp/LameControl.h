//
// Created by jieli51 on 2023/3/3.
//
#include <jni.h>
#include "android/log.h"
#include "IFlytek.h"

#ifdef __cplusplus
extern "C" {
#endif

void lame_control_close(JNIEnv *env, jclass type);

jint lame_control_encode(JNIEnv *env, jclass type,
                         jshortArray buffer_l_, jshortArray buffer_r_, jint samples, jbyteArray mp3buf_);

jint lame_control_encode_interleaved(JNIEnv *env, jclass type,
                                     jshortArray buffer, jint samples, jbyteArray mp3buf_);

jint lame_control_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_);

void lame_control_init(JNIEnv *env, jclass type, jint inSampleRate,
                       jint outChannel, jint outSampleRate, jint outBitrate, jint quality);

#ifdef __cplusplus
}
#endif