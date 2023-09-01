package com.tainzhi.sample.media.camera.gl.textures

import android.graphics.RectF
import android.opengl.GLES20
import com.tainzhi.sample.media.camera.gl.GlUtil
import com.tainzhi.sample.media.camera.gl.Shader
import com.tainzhi.sample.media.camera.gl.ShaderFactory
import com.tainzhi.sample.media.camera.gl.ShaderType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class LineTexture(start: Vertex3F, end: Vertex3F): Texture() {
    private val vertices = floatArrayOf(start.x, start.y, start.z, end.x, end.y, end.z)
    private lateinit var vertexBuffer:  FloatBuffer
    private var color = floatArrayOf(1f, 1f, 1f, 1f)
    private var alpha = 1f
    private var lineWidth = 1f

    override fun load(shaderFactory: ShaderFactory, previewRect: RectF) {
        super.load(shaderFactory, previewRect)
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)
    }

    fun setColor(c: Float) {
    }

    override fun setAlpha(alpha: Float) {
        this.alpha = alpha
    }

    override fun setLineWidth(width: Float) {
        lineWidth = width
    }

    override fun onSetShader(): Shader = shaderFactory.getShader(ShaderType.FRAME)

    override fun onDraw() {
        super.onDraw()
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, GlUtil.COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, GlUtil.VERTEX_STRIDE, vertexBuffer)
        setVec4("u_Color", color)
        setFloat("u_Opacity", alpha)
        GLES20.glLineWidth(lineWidth)
        // 每个顶点3个值，xyz. 此处获取顶点数
        GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertices.size / GlUtil.COORDS_PER_VERTEX)
        GLES20.glDisableVertexAttribArray(mHPosition)
        // reset blend
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
    }

    companion object {
        private val TAG = LineTexture::class.java.simpleName
    }
}