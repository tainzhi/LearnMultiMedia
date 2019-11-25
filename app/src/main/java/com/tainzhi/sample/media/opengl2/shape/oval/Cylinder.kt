package com.renhui.opengles20study.shape.oval

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.tainzhi.sample.media.opengl2.base.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*

/**
 * 圆柱体
 */
class Cylinder : BaseGLSL() {
    private var mProgram = 0
    private val ovalBottom: Oval
    private val ovalTop: Oval
    private val vertexBuffer: FloatBuffer
    private val mViewMatrix = FloatArray(16)
    private val mProjectMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private val n = 360 //切割份数
    private val height = 2.0f //圆锥高度
    private val radius = 1.0f //圆锥底面半径
    private val vSize: Int
    fun onSurfaceCreated() {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        mProgram = createOpenGLProgram(vertexShaderCode, fragmentShaderCode)
    }

    fun onSurfaceChanged(width: Int, height: Int) { //计算宽高比
        val ratio = width.toFloat() / height
        //设置透视投影
        Matrix.frustumM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 20f)
        //设置相机位置
        Matrix.setLookAtM(mViewMatrix, 0, 1.0f, -10.0f, -4.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mViewMatrix, 0)
    }

    fun draw() {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        GLES20.glUseProgram(mProgram)
        Log.e("wuwang", "mProgram:$mProgram")
        val mMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix")
        GLES20.glUniformMatrix4fv(mMatrix, 1, false, mMVPMatrix, 0)
        val mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        Log.e("wuwang", "Get Position:$mPositionHandle")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vSize)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        ovalBottom.setMatrix(mMVPMatrix)
        ovalBottom.draw()
        ovalTop.setMatrix(mMVPMatrix)
        ovalTop.draw()
    }

    companion object {
        private const val vertexShaderCode = "uniform mat4 vMatrix;\n" +
                "varying vec4 vColor;\n" +
                "attribute vec4 vPosition;\n" +
                "void main(){\n" +
                "    gl_Position=vMatrix*vPosition;\n" +
                "    if(vPosition.z!=0.0){\n" +
                "        vColor=vec4(0.0,0.0,0.0,1.0);\n" +
                "    }else{\n" +
                "        vColor=vec4(0.9,0.9,0.9,1.0);\n" +
                "    }\n" +
                "}"
        private const val fragmentShaderCode = "precision mediump float;\n" +
                "varying vec4 vColor;\n" +
                "void main(){\n" +
                "    gl_FragColor=vColor;\n" +
                "}"
    }

    init {
        ovalBottom = Oval()
        ovalTop = Oval(height)
        val pos = ArrayList<Float>()
        val angDegSpan = 360f / n
        run {
            var i = 0f
            while (i < 360 + angDegSpan) {
                pos.add((radius * Math.sin(i * Math.PI / 180f)).toFloat())
                pos.add((radius * Math.cos(i * Math.PI / 180f)).toFloat())
                pos.add(height)
                pos.add((radius * Math.sin(i * Math.PI / 180f)).toFloat())
                pos.add((radius * Math.cos(i * Math.PI / 180f)).toFloat())
                pos.add(0.0f)
                i += angDegSpan
            }
        }
        val d = FloatArray(pos.size)
        for (i in d.indices) {
            d[i] = pos[i]
        }
        vSize = d.size / 3
        val buffer = ByteBuffer.allocateDirect(d.size * 4)
        buffer.order(ByteOrder.nativeOrder())
        vertexBuffer = buffer.asFloatBuffer()
        vertexBuffer.put(d)
        vertexBuffer.position(0)
    }
}