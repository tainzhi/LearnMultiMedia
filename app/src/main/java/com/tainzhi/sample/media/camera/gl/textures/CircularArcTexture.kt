package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES20
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.gl.GlUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class CircularArcTexture(val center: Vertex3F, val radius: Float, val startAngle: Float, val sweepAngle: Float): Texture() {
    private lateinit var vertices :FloatArray
    private var vertexShaderCode = GlUtil.getShaderSource(R.raw.frame_glvs)
    private var fragmentShaderCode = GlUtil.getShaderSource(R.raw.frame_glfs)
    private lateinit var vertexBuffer:  FloatBuffer
    private var color = floatArrayOf(1f, 1f, 1f, 1f)
    private var alpha = 1f
    private var lineWidth = 1f

    override fun onCreate() {
        generateVertices()

        createProgram(vertexShaderCode, fragmentShaderCode)
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)
    }

    fun setColor(c: Float) {
    }

    override fun setLineWidth(width: Float) {
        lineWidth = width
    }

    override fun setAlpha(alpha: Float) {
        this.alpha = alpha
    }

    override fun onDraw() {
        onUseProgram()
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 4x4 matrix
        setMat4("u_ModelMatrix", modelMatrix)
        setMat4("u_ViewMatrix", viewMatrix)
        setMat4("u_ProjectionMatrix", projectionMatrix)
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, GlUtil.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, GlUtil.VERTEX_STRIDE, vertexBuffer)
        setVec4("u_Color", color)
        setFloat("u_Opacity", alpha)
        GLES20.glLineWidth(lineWidth)
        // 每个顶点3个值，xyz. 此处获取顶点数
        GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertices.size / GlUtil.COORDS_PER_VERTEX)
        GLES20.glDisableVertexAttribArray(mHPosition)
        // reset blend
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    private fun generateVertices() {
        val segments = abs(sweepAngle).toInt() + 1
        vertices = FloatArray((segments + 1) * GlUtil.COORDS_PER_VERTEX)
        val angleStep = sweepAngle / segments.toFloat()
        for (i in 0 .. segments) {
            val angleInRadius = (startAngle + angleStep * i)/180f * PI
            vertices[i * GlUtil.COORDS_PER_VERTEX] = (center.x + radius * cos(angleInRadius)).toFloat()
            vertices[i * GlUtil.COORDS_PER_VERTEX + 1] = (center.y + radius * sin(angleInRadius)).toFloat()
            vertices[i * GlUtil.COORDS_PER_VERTEX + 2] = 0f
        }
    }

    companion object {
        private val TAG = CircularArcTexture::class.java.simpleName
    }
}