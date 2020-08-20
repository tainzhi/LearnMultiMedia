package com.tainzhi.sample.media.encodemp4

import android.media.AudioFormat

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/8/20 上午10:13
 * @description:
 **/

class MyMediaRecorder {
    var filePath = ""
    
    // 采样率
    var audioSampleRate = 44100
    
    // 通道
    // 单声道 [android.media.AudioFormat.CHANNEL_IN_MONO]
    // 立体声 [android.media.AudioFormat.CHANNEL_IN_STEREO]
    var audioChannelConfig = AudioFormat.CHANNEL_IN_MONO
    
    // 音频编码率, 采样精读
    var audioBitRate = AudioFormat.ENCODING_PCM_16BIT
    
    // 视频宽
    var videoFrameWidth = 640
    
    // 视频高
    var videoFrameHeight = 480
    
    // 视频编码码率
    var videoBitRate = 600000
    
    // 视频编码帧率
    var videoFrameRate = 30
    var isVertical = 0
    
    var duration = 1000000L
    
    private val aacConsumer = AACEncodeConsumer.Builder()
            .audioSampleRate(audioSampleRate)
            .audioBitRate(audioBitRate)
            .audioChannelConfig(audioChannelConfig)
            .build()
    private val audioPcmProducer = AudioPcmProducer(aacConsumer)
    
    private val h264EncodeConsumer = H264EncodeConsumer.Builder()
            .videoFrameWidth(videoFrameWidth)
            .videoFrameHeight(videoFrameHeight)
            .videoBitRate(videoBitRate)
            .videoFrameRate(videoFrameRate)
            .build()
    private val mediaMuxer = MyMediaMuxer(filePath, duration)
    
    
    fun start() {
        aacConsumer.also {
            it.setTmpuMuxer(mediaMuxer)
        }.start()
        audioPcmProducer.start()
        h264EncodeConsumer.also {
            it.setTmpuMuxer(mediaMuxer)
        }.start()
        
    }
    
    fun addVideoData(frame: ByteArray) {
        h264EncodeConsumer.addData(frame)
    }
    
    fun stop() {
        mediaMuxer.release()
        try {
            aacConsumer.run {
                exit()
                interrupt()
                join()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        try {
            audioPcmProducer.run {
                exit()
                Thread.sleep(500)
                interrupt()
                join()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            h264EncodeConsumer.run {
                exit()
                interrupt()
                join()
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        
    }
}