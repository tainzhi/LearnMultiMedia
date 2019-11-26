package com.tainzhi.sample.media.opengl2.paint

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-25 23:26
 * @description:
 **/

class PaintPointRenderer(private val iCZ: PPgles) : GLSurfaceView.Renderer {
    private var mProgram = 0
    private val mVertexShaderSource: String?
    private val mFragmentShaderSource: String?
    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        mProgram = GLES20.glCreateProgram()
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderSource)
        GLES20.glAttachShader(mProgram, vertexShader)
        GLES20.glAttachShader(mProgram, fragmentShader)
        GLES20.glLinkProgram(mProgram)
        GLES20.glUseProgram(mProgram)
        iCZ.init(mProgram, vertexShader, fragmentShader)
    }

    override fun onSurfaceChanged(gl10: GL10, i: Int, i1: Int) {
        GLES20.glViewport(0, 0, i, i1)
        width = i.toFloat()
        height = i1.toFloat()
    }

    override fun onDrawFrame(gl10: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        iCZ.draw()
    }

    private fun loadShader(type: Int, source: String?): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        var width = 0f
        var height = 0f
    }

    init {
        mVertexShaderSource = iCZ.vertexShader
        mFragmentShaderSource = iCZ.fragmentShader
    }
}