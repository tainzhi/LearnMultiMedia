package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import com.tainzhi.sample.media.camera.gl.BaseGLSurfaceView
import com.tainzhi.sample.media.opengl2.shape.square.Cube
import com.tainzhi.sample.media.opengl2.shape.square.Square
import com.tainzhi.sample.media.opengl2.shape.square.VaryMatrixCube
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:06
 * @description:
 **/

class SquareGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {
    class SquareRenderer : Renderer {
        var square: Square? = null
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            square = Square()
        }
    
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            square!!.onSurfaceChanged(width, height)
        }
    
        override fun onDrawFrame(gl: GL10) {
            square!!.draw()
        }
    }
    
    class CubeRenderer : Renderer {
        var cube: Cube? = null
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            cube = Cube()
            cube!!.onSurfaceCreated()
        }
        
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            cube!!.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            cube!!.draw()
        }
    }
    
    class VaryMatrixCubeRenderer : Renderer {
        var cube: VaryMatrixCube? = null
        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            cube = VaryMatrixCube()
            cube!!.onSurfaceCreated()
        }
        
        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            cube!!.onSurfaceChanged(width, height)
        }

        override fun onDrawFrame(gl: GL10) {
            cube!!.draw()
        }
    }

    init {
        // setRenderer(SquareRenderer()) // 绘制正方形
        setRenderer(CubeRenderer())  // 绘制立方体
        // setRenderer(VaryMatrixCubeRenderer())
    }
}