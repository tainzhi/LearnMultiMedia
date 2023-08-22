package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES20
import com.tainzhi.sample.media.camera.gl.GlUtil

abstract class Texture {
    // 程序句柄
    protected var mProgram = 0

    // 顶点坐标句柄
    protected var mHPosition = 0

    protected var modelMatrix = FloatArray(16)
    protected var viewMatrix = FloatArray(16)
    protected var projectionMatrix = FloatArray(16)

   private var attributeMap = hashMapOf<String, Int>()
    fun create() {
        onCreate()
    }

    fun draw() {
        onDraw()
    }

    open fun setAlpha(alpha: Float) {}

    open fun setLineWidth(alpha: Float) {}

    fun setMatrix(model: FloatArray, view: FloatArray, projection: FloatArray) {
        modelMatrix = model
        viewMatrix = view
        projectionMatrix = projection

    }

    protected abstract fun onCreate()
    protected fun createProgram(vertex: String, fragment: String) {
        mProgram = GlUtil.createOpenGLProgram(vertex, fragment)
        mHPosition = GLES20.glGetAttribLocation(mProgram, "a_Position")
    }

    protected fun onUseProgram() {
        GLES20.glUseProgram(mProgram)
    }

    protected fun onClear() {

    }

    protected open fun onDraw() {
        onClear()
        onUseProgram()
        // 4x4 matrix
        setMat4("u_ModelMatrix", modelMatrix)
        setMat4("u_ViewMatrix", viewMatrix)
        setMat4("u_ProjectionMatrix", projectionMatrix)
    }

    protected fun setMat4(matName: String, mat: FloatArray) {
        if (!attributeMap.containsKey(matName)) {
            attributeMap.put(matName, GLES20.glGetUniformLocation(mProgram, matName))
        }
        GLES20.glUniformMatrix4fv( attributeMap[matName]!!,
            mat.size / 16, false, mat, 0
        )
        GlUtil.checkGlError("set Matrix4 to ${matName}:")
    }

    protected fun setFloat(name: String, value: Float) {
        if (!attributeMap.containsKey(name)) {
            attributeMap.put(name, GLES20.glGetUniformLocation(mProgram, name))
        }
        GLES20.glUniform1f(attributeMap[name]!!, value)
    }


    protected fun setInt(name: String, value: Int) {
        if (!attributeMap.containsKey(name)) {
            attributeMap.put(name, GLES20.glGetUniformLocation(mProgram, name))
        }
        GLES20.glUniform1i(attributeMap[name]!!, value)
    }

    protected fun setVec2(name: String, value: FloatArray) {
        if (!attributeMap.containsKey(name)) {
            attributeMap.put(name, GLES20.glGetUniformLocation(mProgram, name))
        }
        GLES20.glUniform2fv(
            attributeMap[name]!!,
            value.size / 2, value, 0
        )
    }

    protected fun setVec4(name: String, value: FloatArray) {
        if (!attributeMap.containsKey(name)) {
            attributeMap.put(name, GLES20.glGetUniformLocation(mProgram, name))
        }
        GLES20.glUniform4fv(
            attributeMap[name]!!,
            value.size / 4, value, 0
        )
    }

}