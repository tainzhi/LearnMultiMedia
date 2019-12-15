/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* This is a JNI example where we use native methods to play video
 * using the native AMedia* APIs.
 * See the corresponding Java source file located at:
 *
 *   src/com/example/nativecodec/NativeMedia.java
 *
 * In this example we use assert() for "impossible" error conditions,
 * and explicit handling and recovery for more likely error conditions.
 */

#include <assert.h>
#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>

#include "looper.h"
#include "media/NdkMediaCodec.h"
#include "media/NdkMediaExtractor.h"

// for __android_log_print(ANDROID_LOG_INFO, "YourApp", "formatted message");
#include <android/log.h>

#define TAG "NativeCodec"
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

// for native window JNI
#include <android/native_window_jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

typedef struct {
    int fd;
    ANativeWindow *window;
    AMediaExtractor *ex;
    AMediaCodec *codec;
    int64_t renderstart;
    bool sawInputEOS;
    bool sawOutputEOS;
    bool isPlaying;
    bool renderonce;
} workerdata;

workerdata data = {-1, NULL, NULL, NULL, 0, false, false, false, false};

enum {
    kMsgCodecBuffer,
    kMsgPause,
    kMsgResume,
    kMsgPauseAck,
    kMsgDecodeDone,
    kMsgSeek,
};


class mylooper : public looper {
    virtual void handle(int what, void *obj);
};

static mylooper *mlooper = NULL;

int64_t systemnanotime() {
    timespec now;
    clock_gettime(CLOCK_MONOTONIC, &now);
    return now.tv_sec * 1000000000LL + now.tv_nsec;
}

void doCodecWork(workerdata *d) {

    ssize_t bufidx = -1;
    if (!d->sawInputEOS) {
        bufidx = AMediaCodec_dequeueInputBuffer(d->codec, 2000);
        LOGV("input buffer %zd", bufidx);
        if (bufidx >= 0) {
            size_t bufsize;
            auto buf = AMediaCodec_getInputBuffer(d->codec, bufidx, &bufsize);
            auto sampleSize = AMediaExtractor_readSampleData(d->ex, buf, bufsize);
            if (sampleSize < 0) {
                sampleSize = 0;
                d->sawInputEOS = true;
                LOGV("EOS");
            }
            auto presentationTimeUs = AMediaExtractor_getSampleTime(d->ex);

            AMediaCodec_queueInputBuffer(d->codec, bufidx, 0, sampleSize, presentationTimeUs,
                                         d->sawInputEOS ? AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM
                                                        : 0);
            AMediaExtractor_advance(d->ex);
        }
    }

    if (!d->sawOutputEOS) {
        AMediaCodecBufferInfo info;
        auto status = AMediaCodec_dequeueOutputBuffer(d->codec, &info, 0);
        if (status >= 0) {
            if (info.flags & AMEDIACODEC_BUFFER_FLAG_END_OF_STREAM) {
                LOGV("output EOS");
                d->sawOutputEOS = true;
            }
            int64_t presentationNano = info.presentationTimeUs * 1000;
            if (d->renderstart < 0) {
                d->renderstart = systemnanotime() - presentationNano;
            }
            int64_t delay = (d->renderstart + presentationNano) - systemnanotime();
            if (delay > 0) {
                usleep(delay / 1000);
            }
            AMediaCodec_releaseOutputBuffer(d->codec, status, info.size != 0);
            if (d->renderonce) {
                d->renderonce = false;
                return;
            }
        } else if (status == AMEDIACODEC_INFO_OUTPUT_BUFFERS_CHANGED) {
            LOGV("output buffers changed");
        } else if (status == AMEDIACODEC_INFO_OUTPUT_FORMAT_CHANGED) {
            auto format = AMediaCodec_getOutputFormat(d->codec);
            LOGV("format changed to: %s", AMediaFormat_toString(format));
            AMediaFormat_delete(format);
        } else if (status == AMEDIACODEC_INFO_TRY_AGAIN_LATER) {
            LOGV("no output buffer right now");
        } else {
            LOGV("unexpected info code: %zd", status);
        }
    }

    if (!d->sawInputEOS || !d->sawOutputEOS) {
        mlooper->post(kMsgCodecBuffer, d);
    }
}

extern "C" {
// rewind the streaming media player
void
Java_com_example_nativecodec_NativeCodec_rewindStreamingMediaPlayer(JNIEnv *env, jclass clazz) {
    LOGV("@@@ rewind");
    if (mlooper) {
        mlooper->post(kMsgSeek, &data);
    }
}

}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_tainzhi_sample_media_native_1codec_NativeCodecActivity_createStreamingMediaPlayer(
        JNIEnv *env, jobject thiz, jobject asset_mgr, jstring filename) {
    LOGE("@@@ create");

    // convert Java string to UTF-8
    const char *utf8 = env->GetStringUTFChars(filename, NULL);
    LOGV("opening %s", utf8);

    off_t outStart, outLen;
    int fd = AAsset_openFileDescriptor(AAssetManager_open(AAssetManager_fromJava(env, asset_mgr),
                                                          utf8, 0), &outStart, &outLen);
    env->ReleaseStringChars(filename, utf8);
    if (fd < 0) {
        LOGE("failed to open file: %s %d (%s)", utf8, fd, strerror(errno));
        return JNI_FALSE;
    }

    data.fd = fd;

    workerdata *d = &data;

    AMediaExtractor *ex = AMediaExtractor_new();
    media_status_t err = AMediaExtractor_setDataSourceFd(ex, d->fd,
                                                         static_cast<off64_t>(outStart),
                                                         static_cast<off64_t>(outLen));

    close(d->fd);
    if (err != AMEDIA_OK) {
        LOGV()
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_native_1codec_NativeCodecActivity_setPlayingStreamingMediaPlayer(
        JNIEnv *env, jobject thiz, jboolean is_playing) {

}extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_native_1codec_NativeCodecActivity_shutdown(JNIEnv *env,
                                                                         jobject thiz) {
}extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_native_1codec_NativeCodecActivity_setSurface(JNIEnv *env,
                                                                           jobject thiz,
                                                                           jobject surface) {
}extern "C"
JNIEXPORT void JNICALL
Java_com_tainzhi_sample_media_native_1codec_NativeCodecActivity_rewindStreamingMediaPlayer(
        JNIEnv *env, jobject thiz) {
}