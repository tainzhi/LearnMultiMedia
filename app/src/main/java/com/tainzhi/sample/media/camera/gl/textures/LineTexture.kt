package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES20
import android.util.Log
import com.tainzhi.sample.media.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class LineTexture: Texture() {
    private var vertexShaderCode = getShaderSource(R.raw.frame_glvs)
    private var fragmentShaderCode = getShaderSource(R.raw.frame_glfs)

    private val vertexs = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f)
    private lateinit var vertexBuffer:  FloatBuffer
    private var color = floatArrayOf(1f, 1f, 1f, 1f)
    private var alpha = 1f
    private var lineWidth = 1f
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectMatrix = FloatArray(16)
    var mvpMatrix = FloatArray(16)

    override fun onCreate() {
        createProgram(vertexShaderCode, fragmentShaderCode)
        vertexBuffer = ByteBuffer.allocateDirect(vertexs.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertexs).position(0)
    }

    fun setColor() {

    }

    fun setVertices(start: Vertex3F, end: Vertex3F) {
        Log.d(TAG, "setVertices: ")
        vertexs[0] = start.x
        vertexs[1] = start.y
        vertexs[2] = start.z
        vertexs[3] = end.x
        vertexs[4] = end.y
        vertexs[5] = end.z
        vertexBuffer.put(vertexs).position(0)
    }

    override fun onDraw() {
        onUseProgram()
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        checkGlError("LineTexture setBlend")
        // 4x4 matrix
        GLES20.glUniformMatrix4fv(mHMatrix, mvpMatrix.size / 16, false, mvpMatrix, 0)
        checkGlError("LineTexture set Matrix Handle")
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        checkGlError("LineTexture set Position Handle")
        GLES20.glUniform4fv(mHColor, color.size/4, color, 0)
        checkGlError("LineTexture set Color Handle")
        GLES20.glUniform1f(mHOpacity, alpha)
        checkGlError("LineTexture set Alpha Handle")
        GLES20.glLineWidth(lineWidth)
        checkGlError("LineTexture set LineWidth Handle")
        // 每个顶点3个值，xyz. 此处获取顶点数
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexs.size / COORDS_PER_VERTEX)
        checkGlError("LineTexture set DrawLine Handle")
        GLES20.glDisableVertexAttribArray(mHPosition)
        // reset blend
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        checkGlError("LineTexture resetBlend")
    }

    companion object {
        private val TAG = LineTexture::class.java.simpleName
    }
}