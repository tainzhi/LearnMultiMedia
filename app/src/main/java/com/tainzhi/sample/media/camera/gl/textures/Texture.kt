package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES20
import android.opengl.GLES30
import com.tainzhi.sample.media.camera.gl.BaseGLSL

abstract class Texture : BaseGLSL() {
    // 程序句柄
    protected var mProgram = 0

    // 顶点坐标句柄
    protected var mHPosition = 0

    protected var modelMatrix = FloatArray(16)
    protected var viewMatrix = FloatArray(16)
    protected var projectionMatrix = FloatArray(16)
    fun create() {
        onCreate()
    }

    fun draw() {
        onDraw()
    }

    fun setMatrix(model: FloatArray, view: FloatArray, projection: FloatArray) {
        modelMatrix = model
        viewMatrix = view
        projectionMatrix = projection

    }

    protected abstract fun onCreate()
    protected fun createProgram(vertex: String, fragment: String) {
        mProgram = createOpenGLProgram(vertex, fragment)
        mHPosition = GLES20.glGetAttribLocation(mProgram, "a_Position")
    }

    protected fun onUseProgram() {
        GLES30.glUseProgram(mProgram)
    }

    protected abstract fun onDraw()

    protected fun setMat4(matName: String, mat: FloatArray) {
        GLES20.glUniformMatrix4fv(
            GLES20.glGetUniformLocation(mProgram, matName),
            mat.size / 16, false, mat, 0
        )
        checkGlError("set Matrix4 to ${matName}:")
    }

    protected fun setFloat(name: String, value: Float) {
        GLES20.glUniform1f(GLES20.glGetUniformLocation(mProgram, name), value)
    }

    protected fun setInt(name: String, value: Int) {
        GLES20.glUniform1i(GLES20.glGetUniformLocation(mProgram, name), value)
    }

    protected fun setVec4(name: String, value: FloatArray) {
        GLES20.glUniform4fv(
            GLES20.glGetUniformLocation(mProgram, name),
            value.size / 4, value, 0
        )
    }

}