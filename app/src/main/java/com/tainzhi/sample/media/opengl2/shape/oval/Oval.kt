package com.renhui.opengles20study.shape.oval

import android.opengl.GLES20
import android.opengl.Matrix
import com.tainzhi.sample.media.opengl2.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

/**
 * 圆
 */
class Oval @JvmOverloads constructor(height: Float = 0.0f) : BaseGLSL() {
    private val vertexBuffer: FloatBuffer
    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "}"
    private val fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}"
    private val mProgram: Int
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private var mMVPMatrix = FloatArray(16)
    //顶点之间的偏移量
    private val vertexStride = 0 // 每个顶点四个字节
    private var mMatrixHandler = 0
    private var radius = 1.0f
    private val n = 360 //切割份数
    private val shapePos: FloatArray
    private var height = 0.0f
    //设置颜色，依次为红绿蓝和透明通道
    var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    fun setRadius(radius: Float) {
        this.radius = radius
    }

    private fun createPositions(): FloatArray {
        val data = ArrayList<Float>()
        data.add(0.0f) //设置圆心坐标
        data.add(0.0f)
        data.add(height)
        val angDegSpan = 360f / n
        run {
            var i = 0f
            while (i < 360 + angDegSpan) {
                data.add((radius * Math.sin(i * Math.PI / 180f)).toFloat())
                data.add((radius * Math.cos(i * Math.PI / 180f)).toFloat())
                data.add(height)
                i += angDegSpan
            }
        }
        val f = FloatArray(data.size)
        for (i in f.indices) {
            f[i] = data[i]
        }
        return f
    }

    fun onSurfaceChanged(width: Int, height: Int) { //计算宽高比
        val ratio = width.toFloat() / height
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    fun setMatrix(matrix: FloatArray) {
        mMVPMatrix = matrix
    }

    fun draw() { //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram)
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(mColorHandle, 1, color, 0)
        //绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, shapePos.size / 3)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    companion object {
        const val COORDS_PER_VERTEX = 3
    }

    init {
        this.height = height
        shapePos = createPositions()
        val bb = ByteBuffer.allocateDirect(shapePos.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(shapePos)
        vertexBuffer.position(0)
        mProgram = createOpenGLProgram(vertexShaderCode, fragmentShaderCode)
    }
}