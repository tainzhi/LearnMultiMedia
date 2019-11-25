package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.opengl.GLSurfaceView
import com.renhui.opengles20study.shape.oval.Cone
import com.tainzhi.sample.media.opengl2.base.BaseGLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:47
 * @description:  绘制圆锥
 **/

class ConeGLSurfaceView(context: Context?) : BaseGLSurfaceView(context) {
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

    init {
        setRenderer(ConeRenderer())
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}