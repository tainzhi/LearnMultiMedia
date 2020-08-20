package com.tainzhi.sample.media.encodemp4

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.nio.ByteBuffer

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/8/20 上午11:44
 * @description: 采集音频数据
 **/

class AudioPcmProducer(val aacEncodeConsumer: AACEncodeConsumer) : Thread() {
    var audioSampleRate = 44100
    
    // 通道
    // 单声道 [android.media.AudioFormat.CHANNEL_IN_MONO]
    // 立体声 [android.media.AudioFormat.CHANNEL_IN_STEREO]
    var audioChannelConfig = AudioFormat.CHANNEL_IN_MONO
    
    // 音频编码率, 采样精读
    var audioBitRate = AudioFormat.ENCODING_PCM_16BIT
    
    var isExit = false
    
    private var bufferSize: Int = AudioRecord.getMinBufferSize(audioSampleRate, audioChannelConfig, audioBitRate)
    private val audioRecord = AudioRecord(MediaRecorder.AudioSource.MIC,
                                          audioSampleRate,
                                          audioChannelConfig,
                                          audioBitRate,
                                          bufferSize * 2
    )
    
    override fun run() {
        super.run()
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO)
        var bytesRecord = 0
        val tempBuffer = ByteArray(bufferSize)
        try {
            while (!isExit) {
                bytesRecord = audioRecord.read(tempBuffer, 0, bufferSize)
                if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION ||
                        bytesRecord == AudioRecord.ERROR_BAD_VALUE
                ) {
                    continue
                }
                if (bytesRecord != 0 && bytesRecord != -1) {
                    pushDataToConsumer(tempBuffer)
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun pushDataToConsumer(byteArray: ByteArray) {
        aacEncodeConsumer.addData(ByteBuffer.wrap(byteArray), byteArray.size)
    }
    
    fun exit() {
        isExit = true
    }
}