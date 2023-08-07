package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES20
import android.opengl.GLES30
import com.tainzhi.sample.media.camera.gl.BaseGLSL

abstract class Texture: BaseGLSL() {
    // 程序句柄
    protected var mProgram = 0
    // 顶点坐标句柄
    protected var mHPosition = 0
    protected open var mHMatrix = 0
    // 颜色句柄
    protected var mHColor = 0
    // 透明度句柄
    protected var mHOpacity = 0

    fun create() {
        onCreate()
    }

    fun draw() {
        onDraw()
    }
    protected abstract fun onCreate()
    protected fun createProgram(vertex: String, fragment: String) {
        mProgram = createOpenGLProgram(vertex, fragment)
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix")
        mHPosition = GLES20.glGetAttribLocation(mProgram, "a_Position")
        mHColor = GLES20.glGetUniformLocation(mProgram, "u_Color")
        mHOpacity = GLES20.glGetUniformLocation(mProgram, "u_Opacity")
    }

    protected fun onUseProgram() {
        GLES30.glUseProgram(mProgram)
    }

    protected abstract fun onDraw()


}