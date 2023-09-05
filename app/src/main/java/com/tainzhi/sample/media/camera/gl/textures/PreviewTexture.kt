package com.tainzhi.sample.media.camera.gl.textures

import android.graphics.RectF
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.tainzhi.sample.media.camera.gl.Shader
import com.tainzhi.sample.media.camera.gl.ShaderFactory
import com.tainzhi.sample.media.camera.gl.ShaderType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PreviewTexture(
    val textureId: Int,
    val textureSize: Vertex2F,
    var textureMatrix: FloatArray,
    val isTrueAspectRatio: Int,
    previewRectF: RectF

) : Texture() {
    protected var mHTexturePosition = 0

    //顶点坐标
    // 忽略z维度，只保留x,y维度
    private var vertices = floatArrayOf(
        previewRectF.left, previewRectF.top,
        previewRectF.left, previewRectF.bottom,
        previewRectF.right, previewRectF.top,
        previewRectF.right, previewRectF.bottom
    )
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureVertexBuffer: FloatBuffer
    var textureType = 0 //默认使用Texture2D0

    //纹理坐标
    private var textureVertices = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    )

    override fun onSetShader(): Shader = shaderFactory.getShader(ShaderType.CAMERA_PREVIEW)

    override fun load(shaderFactory: ShaderFactory, previewRect: RectF) {
        super.load(shaderFactory, previewRect)
        Log.v("PreviewTexture", "load: previewTexture load")
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVertexBuffer.put(textureVertices).position(0)
        mHTexturePosition = GLES20.glGetAttribLocation(shader.programHandle, "a_TexturePosition")
    }

    override fun unload() {
        super.unload()
    }

    override fun onDraw() {
        super.onDraw()
        setMat4("u_TextureMatrix", textureMatrix)
        setVec2("u_TextureSize", floatArrayOf(textureSize.x, textureSize.y))
        setInt("u_IsTrueAspectRatio", isTrueAspectRatio)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        setInt("u_TextureSampler", textureType)
        GLES20.glEnableVertexAttribArray(programHandle)
        GLES20.glVertexAttribPointer(programHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(mHTexturePosition)
        GLES20.glVertexAttribPointer(
            mHTexturePosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureVertexBuffer
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(programHandle)
        GLES20.glDisableVertexAttribArray(mHTexturePosition)
    }
}