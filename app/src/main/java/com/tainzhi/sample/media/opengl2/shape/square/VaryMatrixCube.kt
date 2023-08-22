package com.tainzhi.sample.media.opengl2.shape.square

import android.opengl.GLES20
import android.opengl.Matrix
import com.tainzhi.sample.media.opengl2.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:40
 * @description:  多种矩阵变换的正方体
 **/

class VaryMatrixCube : BaseGLSL() {
    private val vertexBuffer: FloatBuffer
    private val colorBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val vertexShaderCode = "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "varying  vec4 vColor;" +
            "attribute vec4 aColor;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "  vColor=aColor;" +
            "}"
    private val fragmentShaderCode = "precision mediump float;" +
            "varying vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}"
    private val mProgram: Int
    val cubePositions = floatArrayOf(
            -1.0f, 1.0f, 1.0f,  //正面左上0
            -1.0f, -1.0f, 1.0f,  //正面左下1
            1.0f, -1.0f, 1.0f,  //正面右下2
            1.0f, 1.0f, 1.0f,  //正面右上3
            -1.0f, 1.0f, -1.0f,  //反面左上4
            -1.0f, -1.0f, -1.0f,  //反面左下5
            1.0f, -1.0f, -1.0f,  //反面右下6
            1.0f, 1.0f, -1.0f)
    val index = shortArrayOf(
            6, 7, 4, 6, 4, 5,  //后面
            6, 3, 7, 6, 2, 3,  //右面
            6, 5, 1, 6, 1, 2,  //下面
            0, 3, 2, 0, 2, 1,  //正面
            0, 1, 5, 0, 5, 4,  //左面
            0, 7, 3, 0, 4, 7)
    var color = floatArrayOf(
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            0f, 1f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f,
            1f, 0f, 0f, 1f)
    private var mPositionHandle = 0
    private var mColorHandle = 0
    private var mMVPMatrix: FloatArray? = FloatArray(16)
    private var mMatrixHandler = 0
    fun onSurfaceCreated() { //开启深度测试
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    private val mMatrixCamera = FloatArray(16) //相机矩阵
    private val mMatrixProjection = FloatArray(16) //投影矩阵
    private val mMatrixCurrent = floatArrayOf(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f)
    //设置相机
    fun setCamera(ex: Float, ey: Float, ez: Float, cx: Float, cy: Float, cz: Float, ux: Float, uy: Float, uz: Float) {
        Matrix.setLookAtM(mMatrixCamera, 0, ex, ey, ez, cx, cy, cz, ux, uy, uz)
    }

    fun ortho(left: Float, right: Float, bottom: Float, top: Float, near: Float, far: Float) {
        Matrix.orthoM(mMatrixProjection, 0, left, right, bottom, top, near, far)
    }

    //平移变换
    fun translate(x: Float, y: Float, z: Float) {
        Matrix.translateM(mMatrixCurrent, 0, x, y, z)
    }

    //旋转变换
    fun rotate(angle: Float, x: Float, y: Float, z: Float) {
        Matrix.rotateM(mMatrixCurrent, 0, angle, x, y, z)
    }

    //缩放变换
    fun scale(x: Float, y: Float, z: Float) {
        Matrix.scaleM(mMatrixCurrent, 0, x, y, z)
    }

    val finalMatrix: FloatArray
        get() {
            val ans = FloatArray(16)
            Matrix.multiplyMM(ans, 0, mMatrixCamera, 0, mMatrixCurrent, 0)
            Matrix.multiplyMM(ans, 0, mMatrixProjection, 0, ans, 0)
            return ans
        }

    fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        val rate = width / height.toFloat()
        ortho(-rate * 6, rate * 6, -6f, 6f, 3f, 20f)
        setCamera(0f, 0f, 10f, 0f, 0f, 0f, 0f, 1f, 0f)
        // y轴正方向平移
        translate(0f, 3f, 0f);
// y轴负方向平移，然后按xyz->(0,0,0)到(1,1,1)旋转30度
// translate(0, -3, 0);
// rotate(30f, 1, 1, 1);
// x轴负方向平移，然后按xyz->(0,0,0)到(1,-1,1)旋转120度，在放大到0.5倍
// translate(-3, 0, 0);
// rotate(120f, 1, -1, 1);
        scale(1.5f, 1.5f, 1.5f);
        translate(-0.5f, -2f, 0f)
        scale(1.0f, 1.0f, 1.0f)
        rotate(30f, 1f, 2f, 1f)
    }

    fun draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(mProgram)
        //获取变换矩阵vMatrix成员句柄
        mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        //指定vMatrix的值
        mMVPMatrix = finalMatrix
        if (mMVPMatrix != null) {
            GLES20.glUniformMatrix4fv(mMatrixHandler, 1, false, mMVPMatrix, 0)
        }
        //获取顶点着色器的vPosition成员句柄
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        //获取片元着色器的vColor成员的句柄
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "aColor")
        //设置绘制三角形的颜色
//        GLES20.glUniform4fv(mColorHandle, 2, color, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle)
        GLES20.glVertexAttribPointer(mColorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        //索引法绘制正方体
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    init {
        val bb = ByteBuffer.allocateDirect(cubePositions.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(cubePositions)
        vertexBuffer.position(0)
        val dd = ByteBuffer.allocateDirect(
                color.size * 4)
        dd.order(ByteOrder.nativeOrder())
        colorBuffer = dd.asFloatBuffer()
        colorBuffer.put(color)
        colorBuffer.position(0)
        val cc = ByteBuffer.allocateDirect(index.size * 2)
        cc.order(ByteOrder.nativeOrder())
        indexBuffer = cc.asShortBuffer()
        indexBuffer.put(index)
        indexBuffer.position(0)
        mProgram = createOpenGLProgram(vertexShaderCode, fragmentShaderCode)
    }
}