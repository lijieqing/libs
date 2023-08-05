//
// Created by jieli51 on 2023/3/3.
//
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <jni.h>
#include <cstdlib>
#include "lame/lame.h"
#include "LameControl.h"

static lame_global_flags *glf = NULL;

void lame_control_close(JNIEnv *env, jclass type) {
    lame_close(glf);
    glf = NULL;
}

jint lame_control_encode_interleaved(JNIEnv *env, jclass type,
                                     jshortArray buffer, jint samples, jbyteArray mp3buf_) {
    jshort *buffer_l;
    if (buffer == NULL) {
        LOGE("lame_control_encode_interleaved buffer_l_ is Null");
        buffer_l = NULL;
    } else {
        buffer_l = env->GetShortArrayElements(buffer, NULL);
    }


    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);


    // LOGE("lame_control_encode lame_control_encode_interleaved ");
    int result = lame_encode_buffer_interleaved(glf, buffer_l, samples, (u_char *) mp3buf, mp3buf_size);
    // LOGE("lame_control_encode lame_control_encode_interleaved res %d", result);
    if (buffer != NULL) {
        env->ReleaseShortArrayElements(buffer, buffer_l, 0);
    }
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);

    return result;
}

jint lame_control_encode(JNIEnv *env, jclass type, jshortArray buffer_l_,
                         jshortArray buffer_r_, jint samples, jbyteArray mp3buf_) {
    jshort *buffer_l;
    if (buffer_l_ == NULL) {
        LOGE("lame_control_encode buffer_l_ is Null");
        buffer_l = NULL;
    } else {
        buffer_l = env->GetShortArrayElements(buffer_l_, NULL);
    }

    jshort *buffer_r;
    if (buffer_r_ == NULL) {
        buffer_r = NULL;
        LOGE("lame_control_encode buffer_r_ is Null");
    } else {
        buffer_r = env->GetShortArrayElements(buffer_r_, NULL);
    }

    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);


    // LOGE("lame_control_encode lame_encode_buffer ");
    int result = lame_encode_buffer(glf, buffer_l, buffer_r, samples, (u_char *) mp3buf, mp3buf_size);
    // LOGE("lame_control_encode lame_encode_buffer res %d", result);
    if (buffer_l_ != NULL) {
        env->ReleaseShortArrayElements(buffer_l_, buffer_l, 0);
    }
    if (buffer_r_ != NULL) {
        env->ReleaseShortArrayElements(buffer_r_, buffer_r, 0);
    }
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);

    return result;
}

jint lame_control_flush(JNIEnv *env, jclass type, jbyteArray mp3buf_) {
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    const jsize mp3buf_size = env->GetArrayLength(mp3buf_);

    int result = lame_encode_flush(glf, (u_char *) mp3buf, mp3buf_size);

    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);

    return result;
}

void lame_control_init(JNIEnv *env, jclass type, jint inSampleRate, jint outChannel,
                       jint outSampleRate, jint outBitrate, jint quality) {
    if (glf != NULL) {
        lame_close(glf);
        glf = NULL;
    }
    glf = lame_init();
    lame_set_in_samplerate(glf, inSampleRate);
    lame_set_num_channels(glf, outChannel);
    lame_set_out_samplerate(glf, outSampleRate);
    lame_set_brate(glf, outBitrate);
    lame_set_quality(glf, quality);
    lame_init_params(glf);
}