package com.tainzhi.sample.media.opengl2.shape.triangle

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午3:22
 * @description:
 **/

class ColorfulTriangle : Triangle() {
    private val vertexColorShaderCode = """
        attribute vec4 vPosition;
        uniform mat4 vMatrix;
        varying vec4 vColor;
        attribute vec4 aColor;
        void main() {
            gl_Position = vMatrix * vPosition;
            vColor = aColor;
        }
    """
    private val fragmentColorShaderCode = """
        precision mediump  float;
        varying vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """

    var color = floatArrayOf(
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.3f, 0.5f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f
    )

    private val colorBuffer: FloatBuffer
    private var matrixHandler = 0
    private var positionHander = 0
    private var colorHandler = 0
    private val mvpMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectMatrix = FloatArray(16)

    fun onSurfaceChanged(width: Int, height: Int) {
        val ratio = width.toFloat() / height
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0)
    }

    override fun draw() {
        GLES20.glUseProgram(program)
        matrixHandler = GLES20.glGetUniformLocation(program, "vMatrix")
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0)
        positionHander = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHander)
        GLES20.glVertexAttribPointer(positionHander, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        colorHandler = GLES20.glGetAttribLocation(program, "aColor")
        GLES20.glEnableVertexAttribArray(colorHandler)
        GLES20.glVertexAttribPointer(colorHandler, 4, GLES20.GL_FLOAT, false, 4, colorBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        GLES20.glDisableVertexAttribArray(positionHander)
    }

    init {
        val bb = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)
        val dd = ByteBuffer.allocateDirect(color.size * 4)
        dd.order(ByteOrder.nativeOrder())
        colorBuffer = dd.asFloatBuffer()
        colorBuffer.put(color)
        colorBuffer.position(0)
        program = createOpenGLProgram(vertexColorShaderCode, fragmentColorShaderCode)
        // 申请底色空间
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
    }

}