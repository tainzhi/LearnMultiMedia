package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.view.MotionEvent
import com.tainzhi.sample.media.opengl2.BaseGLSurfaceView
import com.tainzhi.sample.media.opengl2.paint.PaintPoint
import com.tainzhi.sample.media.opengl2.paint.PaintPointRenderer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-25 23:37
 * @description:
 **/

/**
 * 画笔点 GLSurfaceView
 */
class PaintPointGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {
    var mPoint = PaintPoint()
    override fun onTouchEvent(event: MotionEvent): Boolean { // 获取touch的事件
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mPoint.setPosition(x * 2 / PaintPointRenderer.Companion.width - 1.0f, 1.0f - y * 2 / PaintPointRenderer.Companion.height)
                mPoint.setColor(0.0f, 1.0f, 0.0f, 1.0f)
            }
            MotionEvent.ACTION_UP -> {
                mPoint.setPosition(x * 2 / PaintPointRenderer.Companion.width - 1.0f, 1.0f - y * 2 / PaintPointRenderer.Companion.height)
                mPoint.setColor(0.0f, 0.0f, 1.0f, 1.0f)
            }
        }
        return super.onTouchEvent(event)
    }

    init {
        setRenderer(PaintPointRenderer(mPoint))
    }
}