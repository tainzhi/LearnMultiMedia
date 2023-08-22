package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.view.MotionEvent
import com.tainzhi.sample.media.opengl2.BaseGLSurfaceView
import com.tainzhi.sample.media.opengl2.rotate.RotateTriangleRenderer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/26 下午2:05
 * @description:
 **/

class RotateTriangleGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {
    private val mRenderer: RotateTriangleRenderer
    private val TOUCH_SCALE_FACTOR = 180.0f / 320
    private var mPreviousX = 0f
    private var mPreviousY = 0f
    override fun onTouchEvent(e: MotionEvent): Boolean { // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        val x = e.x
        val y = e.y
        when (e.action) {
            MotionEvent.ACTION_MOVE -> {
                var dx = x - mPreviousX
                var dy = y - mPreviousY
                // reverse direction of rotation above the mid-line
                if (y > height / 2) {
                    dx = dx * -1
                }
                // reverse direction of rotation to left of the mid-line
                if (x < width / 2) {
                    dy = dy * -1
                }
                mRenderer.angle = mRenderer.angle + (dx + dy) * TOUCH_SCALE_FACTOR // = 180.0f / 320
                requestRender()
            }
        }
        mPreviousX = x
        mPreviousY = y
        return true
    }

    init {
        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = RotateTriangleRenderer()
        setRenderer(mRenderer)
        // Render the view only when there is a change in the drawing data
        renderMode = RENDERMODE_WHEN_DIRTY
    }
}