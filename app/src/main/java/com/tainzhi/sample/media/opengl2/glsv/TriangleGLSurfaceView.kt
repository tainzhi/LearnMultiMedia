package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.opengl.GLES20
import com.tainzhi.sample.media.opengl2.base.BaseGLSurfaceView
import com.tainzhi.sample.media.opengl2.shape.triangle.CameraTriangle
import com.tainzhi.sample.media.opengl2.shape.triangle.ColorfulTriangle
import com.tainzhi.sample.media.opengl2.shape.triangle.Triangle
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-25 09:08
 * @description:
 **/

class TriangleGLSurfaceView(context: Context) :
    BaseGLSurfaceView(context) {

    init {
        setRenderer(TriangleRenderer())
    }

    class TriangleRenderer : Renderer {

        private lateinit var triangle: Triangle
        override fun onDrawFrame(p0: GL10?) {
            triangle.draw()
        }

        override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
            triangle = Triangle()
        }
    }
}

class CameraTriangleGLSurfaceView(context: Context) :
    BaseGLSurfaceView(context) {

    init {
        setRenderer(CameraTriangleRenderer())
    }

    class CameraTriangleRenderer : Renderer {

        private lateinit var triangle: CameraTriangle
        override fun onDrawFrame(p0: GL10?) {
            triangle.draw()
        }

        override fun onSurfaceChanged(p0: GL10?, p1: Int, p2: Int) {
            triangle.onSurfaceChanged(p1/2, p2/2)
        }

        override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
            triangle = CameraTriangle()
        }
    }
}

class ColorfulTriangleGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {

    init {
        setRenderer(ColorfulTriangleRenderer())
    }

    class ColorfulTriangleRenderer : Renderer {

        private lateinit var triangle: ColorfulTriangle
        override fun onDrawFrame(p0: GL10?) {
            triangle.draw()
        }

        override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
            triangle.onSurfaceChanged(width, height)
        }
        
        override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
            triangle = ColorfulTriangle()
        }
    }

}