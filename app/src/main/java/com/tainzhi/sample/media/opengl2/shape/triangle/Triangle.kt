package com.tainzhi.sample.media.opengl2.shape.triangle

import android.opengl.GLES20
import com.tainzhi.sample.media.camera.gl.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-24 08:51
 * @description:
 **/

open class Triangle : BaseGLSL() {
    var vertexBuffer: FloatBuffer
    var program: Int
    open fun draw() {
        // add program to OpenGLES2.0 context
        GLES20.glUseProgram(program)
        // 获取顶点着色器的vPosition句柄
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        // 启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandle)
        // 准备三角形的坐标数据
        GLES20.glVertexAttribPointer(positionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        // 获取片元着色器的vColor成员的句柄
        var colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        // 绘制三角形的颜色
        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        // 绘制三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount)
        // 禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    companion object {
        // 简单的顶点着色器
        val vertexShaderCode = """
            attribute vec4 vPosition;
            void main() {
                gl_Position = vPosition;
            }
        """
        // 简单的片元着色器
        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;
            void main() {
                gl_FragColor = vColor;
            }
        """

        // 三角形的坐标
        var triangleCoords = floatArrayOf(
                0.5f, 0.5f, 0.0f,
                -0.5f, -0.5f, 0.0f,
                0.5f, -0.5f, 0.0f
        )
        // 三角形的颜色--白色
        var color = floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)
        //  顶点个数
        val vertexCount = triangleCoords.size / COORDS_PER_VERTEX
    }

    init {
        // 申请底色空间
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        val bb = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        // 将坐标数据转成FloatBuffer, 用以传入OpenGL ES program
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)
        program = createOpenGLProgram(vertexShaderCode, Triangle.Companion.fragmentShaderCode)
    }
}