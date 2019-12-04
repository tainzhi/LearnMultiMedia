package com.tainzhi.sample.media.player

import android.os.Handler
import android.os.Message
import com.tainzhi.sample.media.player.PlayTask.LocalHandler.Companion.MSG_PLAY_STOPPED
import java.io.IOException

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
class PlayTask(private val player: MoviePlayer, private val feedback: PlayerFeedback) : Runnable {
    private var doLoop = false
    private var thread: Thread? = null
    private val localHandler: LocalHandler
    private val stopLock = java.lang.Object()
    private var stopped = false

    fun setLoopMode(loopMode: Boolean) {
        doLoop = loopMode
    }

    fun execute() {
        player.setLoopMode(doLoop)
        thread = Thread(this, "Movie Player")
        thread?.start()
    }

    fun requestStop() {
        player.requestStop()
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

    override fun run() {
        try {
            player.play()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } finally {
            synchronized(stopLock) {
                stopped = true
                stopLock.notifyAll()
            }
            localHandler.sendMessage(localHandler.obtainMessage(MSG_PLAY_STOPPED, feedback))
        }
    }

    init {
        localHandler = LocalHandler()
    }

    private class LocalHandler : Handler() {
        override fun handleMessage(msg: Message) {
            val what = msg.what
            when (what) {
                MSG_PLAY_STOPPED -> {
                    val fb = msg.obj as PlayerFeedback
                    fb.playbackStopped()
                }
                else -> throw  RuntimeException("Unknown msg $what")
            }
        }

        companion object {
            const val MSG_PLAY_STOPPED = 0
        }
    }
}