package com.tainzhi.sample.media.player

import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-12-02 20:12
 * @description:
 **/

/**
 * Thread helper for video playback.
 *
 *
 * The PlayerFeedback callbacks will execute on the thread that creates the object,
 * assuming that thread has a looper.  Otherwise, they will execute on the main looper.
 */
class VideoPlayer(sourceFile: String, surface: Surface) {
    private val stopLock = java.lang.Object()
    private var stopped = false

    private lateinit var audioDecoder: AudioDecoder
    private var videoDecoder: VideoDecoder
    private var thread: HandlerThread
    private var handler: Handler
    private var audioThread: HandlerThread
    private var audioHandler: Handler
    private var speedControlCallback = SpeedControlCallback()

    private var onSizeChangedListener: OnSizeChangedListener? = null

    init {
        videoDecoder = VideoDecoder(sourceFile, surface, speedControlCallback)
        audioDecoder = AudioDecoder(sourceFile, speedControlCallback)
        thread = HandlerThread("VideoPlayer")
        thread.start()
        handler = Handler(thread.looper)

        audioThread = HandlerThread("VideoPlayer")
        audioThread.start()
        audioHandler = Handler(audioThread.looper)
    }

    fun setLoop(loopMode: Boolean) {
        videoDecoder.setLoopMode(loopMode)
        audioDecoder.setLoopMode(loopMode)
    }

    fun start() {
        Log.d(TAG, "start()")
        onSizeChangedListener?.onSizeChanged(videoDecoder.videoWidth, videoDecoder.videoHeight)
        handler.post(Runnable {
            videoDecoder.play()
        })

        audioHandler.post(Runnable {
            audioDecoder.play()
        })
    }

    fun stop() {
        Log.d(TAG, "stop()")
        videoDecoder.requestStop()
        thread.quitSafely()
        audioDecoder.requestStop()
        audioThread.quitSafely()
    }

    fun setSpeed(fps: Int) {
        speedControlCallback.setFixedPlaybackRate(fps)
    }


    fun waitForStop() {
        synchronized(stopLock) {
            while (!stopped) {
                try {
                    stopLock.wait()
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    fun setSizeChangedListener(onSizeChangedListener: OnSizeChangedListener) {
        this@VideoPlayer.onSizeChangedListener = onSizeChangedListener
    }

    interface OnSizeChangedListener {
        fun onSizeChanged(videoWidth: Int, videoHeight: Int)
    }

    companion object {
        private const val TAG = "VideoPlayer"
    }

//    override fun run() {
//        try {
//            player.play()
//        } catch (e: IOException) {
//            throw RuntimeException(e)
//        } finally {
//            synchronized(stopLock) {
//                stopped = true
//                stopLock.notifyAll()
//            }
//            localHandler.sendMessage(localHandler.obtainMessage(MSG_PLAY_STOPPED, feedback))
//        }
//    }


//    private class LocalHandler : Handler() {
//        override fun handleMessage(msg: Message) {
//            val what = msg.what
//            when (what) {
//                MSG_PLAY_STOPPED -> {
//                    val fb = msg.obj as PlayerFeedback
//                    fb.playbackStopped()
//                }
//                else -> throw  RuntimeException("Unknown msg $what")
//            }
//        }
//
//        companion object {
//            const val MSG_PLAY_STOPPED = 0
//        }
//    }
}