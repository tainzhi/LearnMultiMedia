package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES20
import com.tainzhi.sample.media.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class GoldenSpiralTexture: Texture() {
    private var vertexShaderCode = getShaderSource(R.raw.frame_glvs)
    private var fragmentShaderCode = getShaderSource(R.raw.frame_glfs)

    private val vertexs = mutableListOf<Float>()
    private lateinit var vertexBuffer:  FloatBuffer
    private var color = floatArrayOf(1f, 1f, 1f, 1f)
    private var alpha = 1f
    private var lineWidth = 1f
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectMatrix = FloatArray(16)
    var mvpMatrix = FloatArray(16)
    private var radius = 540f
    private var z = 1.5f

    override fun onCreate() {
        createProgram(vertexShaderCode, fragmentShaderCode)
        var zstep = 0.01f
        generateSequence(0f, {it + 0.1f})
            .takeWhile { it <= PI * 6 }
            .forEach { i ->
                val x = radius * cos(i)
                val y = radius * sin(i)
                z = z - zstep
                vertexs.add(x)
                vertexs.add(y)
                vertexs.add(z)
            }
        vertexBuffer = ByteBuffer.allocateDirect(vertexs.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertexs.toFloatArray()).position(0)
    }

    fun setColor() {

    }

    override fun onDraw() {
        onUseProgram()
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        checkGlError("LineTexture setBlend")
        // 4x4 matrix
        // GLES20.glUniformMatrix4fv(mHModelMatrix, mvpMatrix.size / 16, false, mvpMatrix, 0)
        checkGlError("LineTexture set Matrix Handle")
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        checkGlError("LineTexture set Position Handle")
        // GLES20.glUniform4fv(mHColor, color.size/4, color, 0)
        // checkGlError("LineTexture set Color Handle")
        // GLES20.glUniform1f(mHOpacity, alpha)
        checkGlError("LineTexture set Alpha Handle")
        GLES20.glLineWidth(lineWidth)
        checkGlError("LineTexture set LineWidth Handle")
        // 每个顶点3个值，xyz. 此处获取顶点数
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexs.size / COORDS_PER_VERTEX)
        checkGlError("LineTexture set DrawLine Handle")
        GLES20.glDisableVertexAttribArray(mHPosition)
        // reset blend
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        checkGlError("LineTexture resetBlend")
    }

    companion object {
        private val TAG = GoldenSpiralTexture::class.java.simpleName
    }
}