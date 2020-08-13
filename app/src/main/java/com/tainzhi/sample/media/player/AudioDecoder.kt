package com.tainzhi.sample.media.player

import android.content.res.AssetFileDescriptor
import android.media.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.IOException

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-12-04 11:36
 * @description:  使用 MediaCodec的自定义 audio decoder
 **/
class AudioDecoder(private val video: AssetFileDescriptor, private val callback: FrameCallback) {
    
    var isStopRequested = false
    var loop = false
    
    var inputBufferSize = 0
    
    private var frameCallback: FrameCallback
    
    init {
        frameCallback = callback
    }

    fun setLoopMode(loopMode: Boolean) {
        loop = loopMode
    }

    fun requestStop() {
        isStopRequested = true
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    fun play() {
        Log.d(TAG, "play")
        val audioExtractor = MediaExtractor()
        try {
            audioExtractor.setDataSource(video)
        } catch (e: IOException) {
            Log.e(TAG, "open asset file with IOException:${e.toString()}")
        }
        // step 1: select track: audio track
        val trackIndex = selectTrack(audioExtractor)
        if (trackIndex < 0) {
            throw RuntimeException("No audio track found in asset file")
        }
        audioExtractor.selectTrack(trackIndex)
        val mediaFormat = audioExtractor.getTrackFormat(trackIndex)
        val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
        val audioChannels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val audioSampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val minBufferSize = AudioTrack.getMinBufferSize(audioSampleRate,
                                                        if (audioChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat
                                                                .CHANNEL_OUT_STEREO,
                                                        AudioFormat.ENCODING_PCM_16BIT);
        val maxInputSize = mediaFormat.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        inputBufferSize = if (minBufferSize > 0) minBufferSize else maxInputSize
        val frameSizeInBytes = audioChannels * 2
        inputBufferSize = (inputBufferSize / frameSizeInBytes) * frameSizeInBytes
        val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder()
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                                            .build())
                .setAudioFormat(AudioFormat.Builder()
                                        .setSampleRate(audioSampleRate)
                                        .setChannelMask(if (audioChannels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO)
                                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                        .build())
                .setBufferSizeInBytes(inputBufferSize)
                .setSessionId(AudioManager.AUDIO_SESSION_ID_GENERATE)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        
        // step 2: generate AudioTrack
        audioTrack.play()
        try {
            val audioDecoder = MediaCodec.createDecoderByType(mime)
            audioDecoder.configure(mediaFormat, null, null, 0)
            // step 3: create MediaCodec for AudioDecoder
            audioDecoder.start()
            doExtract(audioExtractor, trackIndex, audioDecoder, audioTrack)
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
        
        
    }
    
    // step 4: extract
    private fun doExtract(extractor: MediaExtractor, trackIndex: Int, decoder: MediaCodec,
                          audioTrack: AudioTrack) {
        
        // 1 ms = 10^3 us微妙 = 10^6 ns纳秒 = 10^9 ps皮秒
        // 1s = 10^3 ms毫秒 millisecond
        // 1ms = 10^3 us微妙 microsecond
        // 1us = 10^3 ns纳秒 nanosecond
        val timeoutUsec = 10000
        var inputChunk = 0
        var firstInputImeNsec: Long = -1
        var outputDone = false
        var inputDone = false
        val bufferInfo = MediaCodec.BufferInfo()
        
        while (!outputDone) {
            Log.d(TAG, "loop")
            if (VERBOSE) Log.d(TAG, "loop")
            if (isStopRequested) {
                Log.d(TAG, "Stop requested")
                return
            }
            
            if (!inputDone) {
                val inputBufferIndex = decoder.dequeueInputBuffer(timeoutUsec.toLong())
                Log.d(TAG, "inputBufferIndex=$inputBufferIndex")
                if (inputBufferIndex >= 0) {
                    if (firstInputImeNsec == -1L) {
                        firstInputImeNsec = System.nanoTime()
                    }
                    val inputBuf = decoder.getInputBuffer(inputBufferIndex)
                    var chunkSize = 0
                    if (inputBuf != null) {
                        chunkSize = extractor.readSampleData(inputBuf, 0)
                    }
                    if (chunkSize < 0) {
                        decoder.queueInputBuffer(inputBufferIndex, 0, 0, 0L,
                                                 MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                        if (VERBOSE) Log.d(TAG, "send input EOS")
                    } else {
                        if (extractor.sampleTrackIndex != trackIndex) {
                            Log.w(TAG, "WEIRD: got sample from track " +
                                    extractor.sampleTrackIndex + ", expected " + trackIndex)
                        }
                        val presentationTimeUs = extractor.sampleTime
                        decoder.queueInputBuffer(inputBufferIndex, 0, chunkSize,
                                                 presentationTimeUs, 0)
                        if (VERBOSE) {
                            Log.d(TAG, "submitted frame $inputChunk do dec, size=$chunkSize")
                        }
                        inputChunk++
                        extractor.advance()
                    }
                } else {
                    if (VERBOSE) Log.d(TAG, "input buffer not available")
                }
            }
            if (!outputDone) {
                val decoderStatus = decoder.dequeueOutputBuffer(bufferInfo, timeoutUsec.toLong())
                if (decoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) { // no output available yet
                    if (VERBOSE) Log.d(TAG, "no output from decoder available")
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) { // not important for us, since we're using Surface
                    if (VERBOSE) Log.d(TAG, "decoder output buffers changed")
                } else if (decoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val newFormat = decoder.outputFormat
                    if (VERBOSE) Log.d(TAG, "decoder output format changed: $newFormat")
                } else if (decoderStatus < 0) {
                    throw RuntimeException(
                            "unexpected result from decoder.dequeueOutputBuffer: " +
                                    decoderStatus)
                } else {
                    if (firstInputImeNsec != 0L) {
                        val nowNsec = System.nanoTime()
                        Log.d(TAG, "startup lag " + (nowNsec - firstInputImeNsec) / 1000000.0 + "" +
                                " ms")
                        firstInputImeNsec = 0
                    }
                    var doLoop = false
                    if (VERBOSE) Log.d(TAG, "surface decoder given buffer " + decoderStatus +
                            " (size=" + bufferInfo.size + ")")
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        if (VERBOSE) Log.d(TAG, "output EOS")
                        if (loop) {
                            doLoop = true
                        } else {
                            outputDone = true
                        }
                        
                    }
                    val doRender = bufferInfo.size != 0
                    // As soon as we call releaseOutputBuffer, the buffer will be forwarded
                    // to SurfaceTexture to convert to a texture.  We can't control when it
                    // appears on-screen, but we can manage the pace at which we release
                    // the buffers.
                    if (doRender && frameCallback != null) {
                        frameCallback.preRender(bufferInfo.presentationTimeUs)
                    }
                    if (doRender && frameCallback != null) {
                        frameCallback.postRender()
                    }
                    val outputBuffer = decoder.getOutputBuffer(decoderStatus)
                    val tempBuffer = ByteArray(bufferInfo.size)
                    outputBuffer?.position(0)
                    outputBuffer?.get(tempBuffer, 0, bufferInfo.size)
                    outputBuffer?.clear()
                    audioTrack.write(tempBuffer, 0, bufferInfo.size)
                    decoder.releaseOutputBuffer(decoderStatus, doRender)
                    if (doLoop) {
                        Log.d(TAG, "Reached EOS, looping")
                        extractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                        inputDone = false
                        // reset decoder state
                        decoder.flush()
                        frameCallback.loopReset()
                    }
                }
            }
        }
        
        extractor.release()
        decoder.stop()
        decoder.release()
        audioTrack.stop()
        audioTrack.release()
    }

    private fun selectTrack(extractor: MediaExtractor): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track $i ($mime): $format")
                }
                return i
            }
        }
        return -1
    }

    companion object {
        private const val TAG: String = "AudioDecoder"
        private const val VERBOSE = true
    }
}