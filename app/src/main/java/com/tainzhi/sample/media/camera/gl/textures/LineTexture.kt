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

    private val vertices = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f)
    private lateinit var vertexBuffer:  FloatBuffer
    private var color = floatArrayOf(1f, 1f, 1f, 1f)
    private var alpha = 1f
    private var lineWidth = 1f
    private var modelMatrix = FloatArray(16)
    private var viewMatrix = FloatArray(16)
    private var projectionMatrix = FloatArray(16)

    override fun onCreate() {
        createProgram(vertexShaderCode, fragmentShaderCode)
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)
    }

    fun setColor(c: Float) {
    }

    fun setMatrix(model: FloatArray, view: FloatArray, projection:FloatArray) {
        modelMatrix = model
        viewMatrix = view
        projectionMatrix = projection
    }

    fun setVertices(start: Vertex3F, end: Vertex3F) {
        Log.d(TAG, "setVertices: ")
        vertices[0] = start.x
        vertices[1] = start.y
        vertices[2] = start.z
        vertices[3] = end.x
        vertices[4] = end.y
        vertices[5] = end.z
        vertexBuffer.put(vertices).position(0)
    }

    override fun onDraw() {
        onUseProgram()
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        // 4x4 matrix
        setMat4("u_ModelMatrix", modelMatrix)
        setMat4("u_ViewMatrix", viewMatrix)
        setMat4("u_ProjectionMatrix", projectionMatrix)
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer)
        setVec4("u_Color", color)
        setFloat("u_Opacity", alpha)
        GLES20.glLineWidth(lineWidth)
        // 每个顶点3个值，xyz. 此处获取顶点数
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertices.size / COORDS_PER_VERTEX)
        GLES20.glDisableVertexAttribArray(mHPosition)
        // reset blend
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    companion object {
        private val TAG = LineTexture::class.java.simpleName
    }
}