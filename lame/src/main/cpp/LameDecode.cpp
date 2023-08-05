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
#include "LameDecode.h"
#include <vector>

#define ANDROID_BUNDLE_CLASS "android/os/Bundle"

static lame_global_flags *glf = NULL;
static hip_t hipf = NULL;

char *shortToChar(short int shortInt) {
    char *charArray = new char[sizeof(short int)];
    memcpy(charArray, &shortInt, sizeof(short int));
    return charArray;
}

void lame_hip_decode_init(JNIEnv *env, jclass clazz) {
    glf = lame_init();
    lame_set_decode_only(glf, 1);
    hipf = hip_decode_init();
}

void lame_hip_decode_close(JNIEnv *env, jclass clazz) {
    hip_decode_exit(hipf);
    lame_close(glf);
}

void
lame_hip_decode(JNIEnv *env, jclass clazz, jbyteArray mp3buf_, int mp3buf_len, jobject bundle) {

    jint len = env->GetArrayLength(mp3buf_);
    LOGE("lame_decode mp3buf_len:%d, arrayLen:%d", mp3buf_len, len);
    mp3data_struct mp3_data;
    memset(&mp3_data, 0, sizeof(mp3data_struct));

    if (len != mp3buf_len) {
        return;
    }
    short int pcm_l[mp3buf_len], pcm_r[mp3buf_len];
    jbyte *mp3buf = env->GetByteArrayElements(mp3buf_, NULL);

    jclass bundleClass = env->FindClass(ANDROID_BUNDLE_CLASS);
    jmethodID putByteArray = env->GetMethodID(bundleClass, "putShortArray",
                                              "(Ljava/lang/String;[S)V");

    jstring data_pcm = env->NewStringUTF("pcmData");

    std::vector<short> pcm_vector;
    int samples = 0;
    auto buffer = reinterpret_cast<unsigned char *>(mp3buf);
    int mp3_len = mp3buf_len;
    do {
        samples = hip_decode1_headers(hipf, buffer, mp3_len, pcm_l, pcm_r, &mp3_data);
        LOGE("mp3 hip_decode1_headers samples:%d mp3data.header_parsed:%d\n", samples,
             mp3_data.header_parsed);
        if (samples > 0) {
            int channels = -1;
            // 头部解析成功
            if (mp3_data.header_parsed == 1) {
                channels = mp3_data.stereo;
            }
            for (int i = 0; i < samples; i++) {
                pcm_vector.push_back(pcm_l[i]);
                if (channels == 2) {
                    pcm_vector.push_back(pcm_r[i]);
                }
            }
        }
        mp3_len = 0;
    } while (samples > 0);

    if (pcm_vector.size() > 0) {
        auto arr_l = new short[pcm_vector.size()];
        for (int i = 0; i < pcm_vector.size(); ++i) {
            arr_l[i] = pcm_vector[i];
        }
        jshortArray short_Array = env->NewShortArray(pcm_vector.size());
        env->SetShortArrayRegion(short_Array, 0, pcm_vector.size(), arr_l);
        env->CallVoidMethod(bundle, putByteArray, data_pcm, short_Array);
        env->DeleteLocalRef(short_Array);
    }

    env->DeleteLocalRef(data_pcm);
    env->ReleaseByteArrayElements(mp3buf_, mp3buf, 0);
}

/**
 * MP3 file to PCM file
 * @param env
 * @param clazz
 * @param mp3FilePath
 * @param pcmFilePath
 * @return 1: success
 */
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
            LOGE("mp3 hip_decode1_headers samples:%d mp3data.header_parsed:%d\n", samples,
                 mp3data.header_parsed);
            // 头部解析成功
            if (mp3data.header_parsed == 1) {
                nChannels = mp3data.stereo;
            }

            if (samples > 0) {
                for (i = 0; i < samples; i++) {
                    fwrite((char *) &pcm_l[i], sizeof(char), sizeof(pcm_l[i]), pcm);
                    if (nChannels == 2) {
                        fwrite((char *) &pcm_r[i], sizeof(char), sizeof(pcm_r[i]), pcm);
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

    env->ReleaseStringUTFChars(mp3FilePath, mp3Path);
    env->ReleaseStringUTFChars(pcmFilePath, pcmPath);

    return JNI_TRUE;
}
