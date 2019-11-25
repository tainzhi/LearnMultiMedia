package com.tainzhi.sample.media.opengl2.shape.triangle

import android.opengl.GLES20
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-25 09:43
 * @description:  摄像机下的三角形
 **/

class CameraTriangle : Triangle() {
    val viewMatrix = FloatArray(16)
    val projectMatrix = FloatArray(16)
    val mvpMatrix = FloatArray(16)
    var matrixHandler = 0
    var positionHandler = 0
    var colorHandler = 0
    // 颜色, R:G:B:透明值, 当前设置为白色
    var color = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

    fun onSurfaceChanged(width: Int, height: Int) {
        val ratio = width.toFloat() / height
        // 设置透视投影
        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        // 设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // 计算变化矩形
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, viewMatrix, 0)
    }

    override fun draw() {
        GLES20.glUseProgram(program)
        matrixHandler = GLES20.glGetUniformLocation(program, "vMatrix")
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0)
        positionHandler = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandler)
        GLES20.glVertexAttribPointer(positionHandler, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, vertexStride, vertexBuffer)
        colorHandler = GLES20.glGetUniformLocation(program, "vColor")
        GLES20.glUniform4fv(colorHandler, 1, color, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, Triangle.Companion.vertexCount)
        GLES20.glDisableVertexAttribArray(positionHandler)
    }

    companion object {
        const val vertexMatrixShaderCode = """
            attribute vec4 vPosition;
            uniform mat4 vMatrix;
            void main() {
                gl_Position = vMatrix * vPosition;
            }
        """
    }

    init {
        val bb = ByteBuffer.allocateDirect(Triangle.Companion.triangleCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(Triangle.Companion.triangleCoords)
        vertexBuffer.position(0)
        program = createOpenGLProgram(vertexMatrixShaderCode, Triangle.Companion.fragmentShaderCode)
    }
}