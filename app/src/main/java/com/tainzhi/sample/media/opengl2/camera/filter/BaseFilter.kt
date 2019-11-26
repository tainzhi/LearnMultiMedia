package com.tainzhi.sample.media.opengl2.camera.filter

import android.opengl.GLES20
import android.util.SparseArray
import com.tainzhi.sample.media.opengl2.base.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

/**
 * 基础颜色过滤器
 */
abstract class BaseFilter : BaseGLSL() {
    /**
     * 程序句柄
     */
    protected var mProgram = 0
    /**
     * 顶点坐标句柄
     */
    protected var mHPosition = 0
    /**
     * 纹理坐标句柄
     */
    protected var mHCoord = 0
    /**
     * 总变换矩阵句柄
     */
    protected open var mHMatrix = 0
    /**
     * 默认纹理贴图句柄
     */
    protected var mHTexture = 0
    /**
     * 顶点坐标Buffer
     */
    protected var mVerBuffer: FloatBuffer? = null
    /**
     * 纹理坐标Buffer
     */
    protected var mTexBuffer: FloatBuffer? = null
    open var flag = 0
    open var matrix = Arrays.copyOf(OM, 16)
    var textureType = 0 //默认使用Texture2D0
    var textureId = 0
    //顶点坐标
    private val pos = floatArrayOf(
            -1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f)
    //纹理坐标
    private val coord = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f)
    private val mBools: SparseArray<BooleanArray>? = null
    private val mInts: SparseArray<IntArray>? = null
    private val mFloats: SparseArray<FloatArray>? = null
    fun create() {
        onCreate()
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
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition")
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoord")
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture")
    }

    /**
     * Buffer初始化
     */
    protected open fun initBuffer() {
        val a = ByteBuffer.allocateDirect(32)
        a.order(ByteOrder.nativeOrder())
        mVerBuffer = a.asFloatBuffer()
        mVerBuffer?.put(pos)
        mVerBuffer?.position(0)
        val b = ByteBuffer.allocateDirect(32)
        b.order(ByteOrder.nativeOrder())
        mTexBuffer = b.asFloatBuffer()
        mTexBuffer?.put(coord)
        mTexBuffer?.position(0)
    }

    protected fun onUseProgram() {
        GLES20.glUseProgram(mProgram)
    }

    /**
     * 启用顶点坐标和纹理坐标进行绘制
     */
    protected fun onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer)
        GLES20.glEnableVertexAttribArray(mHCoord)
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(mHCoord)
    }

    /**
     * 清除画布
     */
    protected open fun onClear() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
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

    init {
        initBuffer()
    }
}