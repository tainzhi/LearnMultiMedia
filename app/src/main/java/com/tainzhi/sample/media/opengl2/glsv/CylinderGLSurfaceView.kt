package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.opengl.GLSurfaceView
import com.renhui.opengles20study.shape.oval.Cylinder
import com.tainzhi.sample.media.opengl2.BaseGLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:47
 * @description:  绘制圆形的GLSurfaceView
 **/

class CylinderGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {
    
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


    init {
        setRenderer(CylinderRenderer())
        renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
    }
}