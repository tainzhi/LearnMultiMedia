package com.tainzhi.sample.media.player

import android.content.res.AssetFileDescriptor
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Build
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import java.io.IOException

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-12-02 19:28
 * @description:  使用 MediaCodec的自定义 video decoder
 **/

class VideoDecoder(
        val video: AssetFileDescriptor, private val outputSurface: Surface,
        private var frameCallback: FrameCallback) {
    var isStopRequested = false
    var loop = false
    
    var videoWidth = 0
    var videoHeight = 0
    
    fun setLoopMode(loopMode: Boolean) {
        loop = loopMode
    }

    fun requestStop() {
        isStopRequested = true
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    fun play() {
        var extractor: MediaExtractor? = null
        var decoder: MediaCodec? = null
        
        try {
            extractor = MediaExtractor()
            extractor.setDataSource(video)
            val trackIndex = selectTrack(extractor)
            if (trackIndex < 0) {
                throw RuntimeException("No video track found in asset file")
            }
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val mime = format.getString(MediaFormat.KEY_MIME)
            decoder = MediaCodec.createDecoderByType(mime!!)
            decoder.configure(format, outputSurface, null, 0)
            decoder.start()
            doExtract(extractor, trackIndex, decoder, frameCallback)
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    private fun doExtract(extractor: MediaExtractor, trackIndex: Int, decoder: MediaCodec,
                          frameCallback: FrameCallback) {
        // operates efficiently without delays on the output side.
        //
        // To avoid delays on the output side, we need to keep the codec's input buffers
        // fed.  There can be significant latency between submitting frame N to the decoder
        // and receiving frame N on the output, so we need to stay ahead of the game.
        //
        // Many video decoders seem to want several frames of video before they start
        // producing output -- one implementation wanted four before it appeared to
        // configure itself.  We need to provide a bunch of input frames up front, and try
        // to keep the queue full as we go.
        //
        // (Note it's possible for the encoded data to be written to the stream out of order,
        // so we can't generally submit a single frame and wait for it to appear.)
        //
        // We can't just fixate on the input side though.  If we spend too much time trying
        // to stuff the input, we might miss a presentation deadline.  At 60Hz we have 16.7ms
        // between frames, so sleeping for 10ms would eat up a significant fraction of the
        // time allowed.  (Most video is at 30Hz or less, so for most content we'll have
        // significantly longer.)  Waiting for output is okay, but sleeping on availability
        // of input buffers is unwise if we need to be providing output on a regular schedule.
        //
        //
        // In some situations, startup latency may be a concern.  To minimize startup time,
        // we'd want to stuff the input full as quickly as possible.  This turns out to be
        // somewhat complicated, as the codec may still be starting up and will refuse to
        // accept input.  Removing the timeout from dequeueInputBuffer() results in spinning
        // on the CPU.
        //
        // If you have tight startup latency requirements, it would probably be best to
        // "prime the pump" with a sequence of frames that aren't actually shown (e.g.
        // grab the first 10 NAL units and shove them through, then rewind to the start of
        // the first key frame).
        //
        // The actual latency seems to depend on strongly on the nature of the video (e.g.
        // resolution).
        //
        //
        // One conceptually nice approach is to loop on the input side to ensure that the codec
        // always has all the input it can handle.  After submitting a buffer, we immediately
        // check to see if it will accept another.  We can use a short timeout so we don't
        // miss a presentation deadline.  On the output side we only check once, with a longer
        // timeout, then return to the outer loop to see if the codec is hungry for more input.
        //
        // In practice, every call to check for available buffers involves a lot of message-
        // passing between threads and processes.  Setting a very brief timeout doesn't
        // exactly work because the overhead required to determine that no buffer is available
        // is substantial.  On one device, the "clever" approach caused significantly greater
        // and more highly variable startup latency.
        //
        // The code below takes a very simple-minded approach that works, but carries a risk
        // of occasionally running out of output.  A more sophisticated approach might
        // detect an output timeout and use that as a signal to try to enqueue several input
        // buffers on the next iteration.
        //
        // If you want to experiment, set the VERBOSE flag to true and watch the behavior
        // in logcat.  Use "logcat -v threadtime" to see sub-second timing.

        val timeoutUsec = 10000
        var inputChunk = 0
        var firstInputImeNsec: Long = -1
        var outputDone = false
        var inputDone = false
        val bufferInfo = MediaCodec.BufferInfo()
        while (!outputDone) {
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
                    decoder.releaseOutputBuffer(decoderStatus, doRender)
                    if (doRender && frameCallback != null) {
                        frameCallback.postRender()
                    }
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
    }

    private fun selectTrack(extractor: MediaExtractor): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                if (VERBOSE) {
                    Log.d(TAG, "Extractor selected track $i ($mime): $format")
                }
                return i
            }
        }
        return -1
    }

    companion object {
        private const val TAG: String = "VideoDecoder"
        private val VERBOSE = false
    }

    init {
        // Pop the file open and pull out the video characteristics.
        // TODO: consider leaving the extractor open.  Should be able to just seek back to
        //       the start after each iteration of play.  Need to rearrange the API a bit --
        //       currently play() is taking an all-in-one open+work+release approach.
        var extractor: MediaExtractor? = null
        try {
            extractor = MediaExtractor()
            extractor!!.setDataSource(video)
            val trackIndex = selectTrack(extractor!!)
            if (trackIndex < 0) {
                throw RuntimeException("No video track found in asset file")
            }
            extractor!!.selectTrack(trackIndex)
            val format = extractor!!.getTrackFormat(trackIndex)
            videoWidth = format.getInteger(MediaFormat.KEY_WIDTH)
            videoHeight = format.getInteger(MediaFormat.KEY_HEIGHT)
            if (VERBOSE) {
                Log.d(TAG, "Video size is " + videoWidth + "x" + videoHeight)
            }
        } finally {
            if (extractor != null) {
                extractor!!.release()
            }
        }
    }

}