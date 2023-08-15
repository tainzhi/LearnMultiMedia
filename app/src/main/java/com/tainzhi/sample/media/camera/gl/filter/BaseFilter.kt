package com.tainzhi.sample.media.camera.gl.filter

import android.opengl.GLES20
import android.util.SparseArray
import com.tainzhi.sample.media.camera.gl.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.Arrays

/**
 * 基础颜色过滤器
 */
abstract class BaseFilter : BaseGLSL() {
    protected var mProgram = 0
    protected var mHPosition = 0
    protected open var mHMatrix = 0
    protected var mHTexturePosition = 0
    protected var mHTexture = 0
    //顶点坐标
    // 忽略z维度，只保留x,y维度
    private var vertices = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    protected lateinit var vertexBuffer: FloatBuffer
    protected lateinit var textureVertexBuffer: FloatBuffer
    open var flag = 0
    open var matrix = Arrays.copyOf(OM, 16)
    var textureType = 0 //默认使用Texture2D0
    var textureId = 0
    //纹理坐标
    private var textureVertices = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f)
    private val mBools: SparseArray<BooleanArray>? = null
    private val mInts: SparseArray<IntArray>? = null
    private val mFloats: SparseArray<FloatArray>? = null
    fun create() {
        onCreate()
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVertexBuffer.put(textureVertices).position(0)
    }

    fun setVertices(width: Float, height: Float) {
        vertices = floatArrayOf(
            0f, 0f,
            0f, height,
            width, 0f,
            width, height
        )
        vertexBuffer.put(vertices).position(0)
    }

    fun setTextureVertices(width: Float, height: Float) {
        textureVertices = floatArrayOf(
            0.0f, 0.0f,
            0.0f, height,
            width, 0.0f,
            width, height)
        textureVertexBuffer.put(textureVertices).position(0)
    }

    fun setSize(width: Int, height: Int) {
        onSizeChanged(width, height)
    }

    open fun draw() {
        onClear()
        onUseProgram()
        onSetExpandData()
        onBindTexture()
        onDraw()
    }

    // 需要继承子类的后，实现其逻辑，基类默认返回-1
    open val outputTexture: Int
        get() = -1

    /**
     * 实现此方法，完成程序的创建，可直接调用createProgram来实现
     */
    protected abstract fun onCreate()

    protected abstract fun onSizeChanged(width: Int, height: Int)
    protected fun createProgram(vertex: String, fragment: String) {
        mProgram = createOpenGLProgram(vertex, fragment)
        mHPosition = GLES20.glGetAttribLocation(mProgram, "a_Position")
        mHTexturePosition = GLES20.glGetAttribLocation(mProgram, "a_TexturePosition")
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "u_Matrix")
        mHTexture = GLES20.glGetUniformLocation(mProgram, "u_Texture")
    }

    protected fun onUseProgram() {
        GLES20.glUseProgram(mProgram)
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected fun onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(mHTexturePosition)
        GLES20.glVertexAttribPointer(mHTexturePosition, 2, GLES20.GL_FLOAT, false, 0, textureVertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(mHTexturePosition)
    }

    /**
     * 清除画布
     */
    protected open fun onClear() {
    }

    /**
     * 设置其他扩展数据
     */
    protected open fun onSetExpandData() {
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, matrix, 0)
    }

    /**
     * 绑定默认纹理
     */
    protected open fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(mHTexture, textureType)
    }

    companion object {
        /**
         * 单位矩阵
         */
        val OM = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
    }
}