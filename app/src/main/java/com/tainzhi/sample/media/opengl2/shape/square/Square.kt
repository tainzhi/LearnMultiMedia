package com.tainzhi.sample.media.opengl2.shape.square

import android.opengl.GLES20
import android.opengl.Matrix
import com.tainzhi.sample.media.camera.gl.BaseGLSL
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/25 下午4:09
 * @description:
 **/

class Square : BaseGLSL() {
    val vertexBuffer: FloatBuffer
    val indexBuffer: ShortBuffer
    val vertexShaderCode = """
        attribute vec4 vPosition;
        uniform mat4 vMatrix;
        void main() {
            gl_Position = vMatrix * vPosition;
            }
    """.trimIndent()
    val fragmentShaderCode = """
        precision mediump float;
        uniform vec4 vColor;
        void main() {
            gl_FragColor = vColor;
        }
    """.trimIndent()

    val program: Int
    var positionHandler = 0
    var colorHandler = 0
    val modelMatrix = FloatArray(16)
    val viewMatrix = FloatArray(16)
    val projectMatrix = FloatArray(16)
    val mvpMatrix = FloatArray(16)

    val vertexStride = COORDS_PER_VERTEX * 4 //每个顶点4个字节
    var matrixHandler = 0
    var color = floatArrayOf(1.0f, 0.0f, 1.0f, 1.0f)

    fun onSurfaceChanged(width: Int, height: Int) {
        val ratio = width.toFloat() / height
        //设置透视投影
//        Matrix.frustumM(projectMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
        Matrix.setIdentityM(modelMatrix, 0)
//        Matrix.translateM(modelMatrix, 0, 0f, (height - width)/2f, 0f);
//        Matrix.orthoM(projectMatrix, 0, 0f, width.toFloat(), 0f, height.toFloat(), 3f, 7f)
//        正交投影，y轴指向下方，那么最终的坐标原点在左上角，与android view坐标系相同
        Matrix.orthoM(projectMatrix, 0, 0f, width.toFloat(), height.toFloat(), 0f, 3f, 7f)
        //设置相机位置
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 7.0f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        //计算变换矩阵
        Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(mvpMatrix, 0, projectMatrix, 0, mvpMatrix, 0)
    }

    fun draw() { //将程序加入到OpenGLES2.0环境
        GLES20.glUseProgram(program)
        //获取变换矩阵vMatrix成员句柄
        matrixHandler = GLES20.glGetUniformLocation(program, "vMatrix")
        //指定vMatrix的值
        GLES20.glUniformMatrix4fv(matrixHandler, 1, false, mvpMatrix, 0)
        //获取顶点着色器的vPosition成员句柄
        positionHandler = GLES20.glGetAttribLocation(program, "vPosition")
        //启用三角形顶点的句柄
        GLES20.glEnableVertexAttribArray(positionHandler)
        //准备三角形的坐标数据
        GLES20.glVertexAttribPointer(positionHandler, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer)
        //获取片元着色器的vColor成员的句柄
        colorHandler = GLES20.glGetUniformLocation(program, "vColor")
        //设置绘制三角形的颜色
        GLES20.glUniform4fv(colorHandler, 1, color, 0)
        //索引法绘制正方形
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, index.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)
        //禁止顶点数组的句柄
        GLES20.glDisableVertexAttribArray(positionHandler)
    }

    companion object {
        var triangleCoords = floatArrayOf(

//            -0.5f, 0.5f, 0.0f,  // top left
//            -0.5f, -0.5f, 0.0f,  // bottom left
//            0.5f, -0.5f, 0.0f,  // bottom right
//            0.5f, 0.5f, 0.0f // top right
            0f, 1080f, 0f,
            0f, 0f, 0f,
            1080f, 0f, 0f,
            1080f, 1080f, 0f
        )
        var index = shortArrayOf(
                0, 1, 2, 0, 3, 2
        )
    }

    init {
        val bb = ByteBuffer.allocateDirect(triangleCoords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer.put(triangleCoords)
        vertexBuffer.position(0)
        val cc = ByteBuffer.allocateDirect(index.size * 2)
        cc.order(ByteOrder.nativeOrder())
        indexBuffer = cc.asShortBuffer()
        indexBuffer.put(index)
        indexBuffer.position(0)
        program = createOpenGLProgram(vertexShaderCode, fragmentShaderCode)
    }


}