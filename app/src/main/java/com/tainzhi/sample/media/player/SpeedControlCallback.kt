package com.tainzhi.sample.media.player

import android.util.Log
import kotlin.math.absoluteValue

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-12-02 21:42
 * @description:
 **/


/**
 * Movie player callback.
 *
 *
 * The goal here is to play back frames at the original rate.  This is done by introducing
 * a pause before the frame is submitted to the renderer.
 *
 *
 * This is not coordinated with VSYNC.  Since we can't control the display's refresh rate, and
 * the source material has time stamps that specify when each frame should be presented,
 * we will have to drop or repeat frames occasionally.
 *
 *
 * Thread restrictions are noted in the method descriptions.  The FrameCallback overrides should
 * only be called from the VideoDecoder.
 */
class SpeedControlCallback : FrameCallback {
    var prevPresentUsec: Long = 0
    var prevMonoUsec: Long = 0
    var fixedFrameDurationUsec: Long = ONE_MILLION / 30
    var loopReset = false

    fun setFixedPlaybackRate(fps: Int) {
        fixedFrameDurationUsec = ONE_MILLION / fps
    }


    override fun preRender(presentationTimeUsec: Long) {
        Log.d(TAG, "preRender(), prevMonoUsec=$prevMonoUsec")
        // For the first frame, we grab the presentation time from the video
        // and the current monotonic clock time.  For subsequent frames, we
        // sleep for a bit to try to ensure that we're rendering frames at the
        // pace dictated by the video stream.
        //
        // If the frame rate is faster than vsync we should be dropping frames.  On
        // Android 4.4 this may not be happening.
        if (prevMonoUsec == 0L) {
            prevMonoUsec = System.nanoTime() / 1000
            prevPresentUsec = presentationTimeUsec
        } else {
            // compute the desired time delta between the previous frame and this frame
            var frameDelta: Long
            if (loopReset) {
                // We don't get an indication of how long the last frame should appear
                // on-screen, so we just throw a reasonable value in.  We could probably
                // do better by using a previous frame duration or some sort of average;
                // for now we just use 30fps.
                prevPresentUsec = presentationTimeUsec - ONE_MILLION / 30
                loopReset = false
            }
            frameDelta = if (fixedFrameDurationUsec != 0L) fixedFrameDurationUsec else {
                presentationTimeUsec - prevPresentUsec
            }
            if (frameDelta < 0) {
                Log.w(TAG, "Weird, video times went backward")
                frameDelta = 0
            } else if (frameDelta == 0L) {
                Log.i(TAG, "Warning: current frame and previous frame had same timestamp")
            } else if (frameDelta > 10 * ONE_MILLION) {
                // Inter-frame times could be arbitrarily long.  For this player, we want
                // to alert the developer that their movie might have issues (maybe they
                // accidentally output timestamps in nsec rather than usec).
                Log.i(TAG, "Inter-frame pause was " + frameDelta / ONE_MILLION +
                        "sec, capping at 5 sec")
                frameDelta = 5 * ONE_MILLION
            }
            // when we want to wake up
            val desiredUsec = prevMonoUsec + frameDelta
            var nowUsec = System.nanoTime() / 1000
            while (nowUsec < (desiredUsec - 100)) {
                // Sleep until it's time to wake up.  To be responsive to "stop" commands
                // we're going to wake up every half a second even if the sleep is supposed
                // to be longer (which should be rare).  The alternative would be
                // to interrupt the thread, but that requires more work.
                //
                // The precision of the sleep call varies widely from one device to another;
                // we may wake early or late.  Different devices will have a minimum possible
                // sleep time. If we're within 100us of the target time, we'll probably
                // overshoot if we try to sleep, so just go ahead and continue on.
                var sleepTimeUsec = desiredUsec - nowUsec
                if (sleepTimeUsec > 500000) {
                    sleepTimeUsec = 500000
                }
                try {
                    if (CHECK_SLEEP_TIME) {
                        val startNsec = System.nanoTime()
                        Thread.sleep(sleepTimeUsec / 1000, (sleepTimeUsec % 1000).toInt() * 1000)
                        val actualSleepNsec = System.nanoTime() - startNsec
                        Log.d(TAG, "sleep=" + sleepTimeUsec + " actual=" + actualSleepNsec / 1000 +
                                " diff=" + (actualSleepNsec / 1000 - sleepTimeUsec).absoluteValue +
                                " (usec)")

                    } else {
                        Thread.sleep(sleepTimeUsec / 1000, (sleepTimeUsec % 1000).toInt() * 1000)
                    }
                } catch (ie: InterruptedException) {
                    throw RuntimeException(ie)
                }
                nowUsec = System.nanoTime() / 1000
            }
            prevMonoUsec += frameDelta
            prevPresentUsec += frameDelta
        }
    }

    override fun postRender() {
        Log.d(TAG, "postRender()")
    }

    override fun loopReset() {
        loopReset = true
    }

    companion object {
        private const val TAG: String = "SpeedControlCallback"
        private const val CHECK_SLEEP_TIME = true
        private const val ONE_MILLION = 1000000L
    }
}