package com.tainzhi.sample.media.player

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-12-02 19:30
 * @description:
 **/

interface FrameCallback {
    /**
     * Called immediately before the frame is rendered.
     * @param presentationTimeUsec The desired presentation time, in microseconds.
     */
    fun preRender(presentationTimeUsec: Long)

    /**
     * Called immediately after the frame render call returns.  The frame may not have
     * actually been rendered yet.
     * TODO: is this actually useful?
     */
    fun postRender()

    /**
     * Called after the last frame of a looped movie has been rendered.  This allows the
     * callback to adjust its expectations of the next presentation time stamp.
     */
    fun loopReset()
}