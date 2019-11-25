package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.opengl.GLSurfaceView
import com.renhui.opengles20study.shape.oval.Ball
import com.renhui.opengles20study.shape.oval.BallWithLight
import com.tainzhi.sample.media.opengl2.base.BaseGLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:47
 * @description:  绘制圆形的GLSurfaceView
 **/

class BallGLSurfaceView(context: Context?) : BaseGLSurfaceView(context) {
    /**
     * 球体渲染器
     */
    internal inner class BallRenderer : Renderer {
        var ball = Ball()
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            ball.onSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            ball.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            ball.draw()
        }
    }

    /**
     * 带光源球体渲染器
     */
    internal inner class BallWithLightRenderer : Renderer {
        var ballWithLight = BallWithLight()
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            ballWithLight.onSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            ballWithLight.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            ballWithLight.draw()
        }
    }

    init {
//        setRenderer(CylinderRenderer())
        setRenderer(BallWithLightRenderer())
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}