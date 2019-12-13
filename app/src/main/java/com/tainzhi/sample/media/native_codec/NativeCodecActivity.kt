package com.tainzhi.sample.media.native_codec

import android.app.Activity
import android.content.res.AssetManager
import android.os.Bundle
import android.view.Surface

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/12/13 下午10:30
 * @description:
 **/

class NativeCodecActivity: Activity() {

    external fun createStreamingMediaPlayer(assetMgr: AssetManager?, filename: String?): Boolean
    external fun setPlayingStreamingMediaPlayer(isPlaying: Boolean)
    external fun shutdown()
    external fun setSurface(surface: Surface?)
    external fun rewindStreamingMediaPlayer()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    init {
        System.loadLibrary("native-codec-jni")
    }
}
