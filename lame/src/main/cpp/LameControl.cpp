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

//可以不传pcmPath，不要保存pcm文件
int lame_mp3_to_pcm(JNIEnv *env, jclass clazz, jstring mp3FilePath, jstring pcmFilePath) {
    // 获取 MP3 文件的路径
    const char *mp3Path = env->GetStringUTFChars(mp3FilePath, NULL);

    // 获取 PCM 文件的路径
    const char *pcmPath = env->GetStringUTFChars(pcmFilePath, NULL);

    int read, i, samples;
    long cumulative_read = 0;
    const int PCM_SIZE = 8192;
    const int MP3_SIZE = 8192;

    // 输出左右声道
    short int pcm_l[PCM_SIZE], pcm_r[PCM_SIZE];
    unsigned char mp3_buffer[MP3_SIZE];

    //input输入MP3文件
    FILE *mp3 = fopen(mp3Path, "rb");
    FILE *pcm = fopen(pcmPath, "wb");
    fseek(mp3, 0, SEEK_SET);

    lame_global_flags *lame = lame_init();
    lame_set_decode_only(lame, 1);

    hip_t hip = hip_decode_init();

    mp3data_struct mp3data;
    memset(&mp3data, 0, sizeof(mp3data));

    int nChannels = -1;
    int mp3_len;

    while ((read = fread(mp3_buffer, sizeof(char), MP3_SIZE, mp3)) > 0) {
        // LOGE("mp3 read len:%d\n", read);
        mp3_len = read;
        cumulative_read += read * sizeof(char);
        do {
            samples = hip_decode1_headers(hip, mp3_buffer, mp3_len, pcm_l, pcm_r, &mp3data);
            // LOGE("mp3 hip_decode1_headers samples:%d mp3data.header_parsed:%d\n", samples, mp3data.header_parsed);
            // 头部解析成功
            if (mp3data.header_parsed == 1) {
                nChannels = mp3data.stereo;
            }

            if (samples > 0) {
                for (i = 0; i < samples; i++) {
                    int writeRes = fwrite((char *) &pcm_l[i], sizeof(char), sizeof(pcm_l[i]), pcm);
                    // LOGE("mp3 fwrite left:%d\n", writeRes);
                    if (nChannels == 2) {
                        writeRes = fwrite((char *) &pcm_r[i], sizeof(char), sizeof(pcm_r[i]), pcm);
                        // LOGE("mp3 fwrite right:%d\n", writeRes);
                    }
                }
            }
            mp3_len = 0;
        } while (samples > 0);
    }

    hip_decode_exit(hip);
    lame_close(lame);
    fclose(mp3);
    fclose(pcm);

    return JNI_TRUE;
}
