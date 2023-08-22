package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.opengl.GLSurfaceView
import com.renhui.opengles20study.shape.oval.*
import com.tainzhi.sample.media.opengl2.BaseGLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:47
 * @description:  绘制圆形的GLSurfaceView
 **/

class OvalGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {
    internal inner class OvalRenderer : Renderer {
        var oval: Oval? = null
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            oval = Oval()
        }
        
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            oval!!.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            oval!!.draw()
        }
    }

    internal inner class ConeRenderer : Renderer {
        var cone = Cone()
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            cone.onSurfaceCreate()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            cone.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            cone.draw()
        }
    }

    /**
     * 圆柱体渲染器
     */
    internal inner class CylinderRenderer : Renderer {
        var cylinder = Cylinder()
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            cylinder.onSurfaceCreated()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            cylinder.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            cylinder.draw()
        }
    }

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
//        setRenderer(OvalRenderer())
//        setRenderer(ConeRenderer())
//        setRenderer(CylinderRenderer())
        setRenderer(BallWithLightRenderer())
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}