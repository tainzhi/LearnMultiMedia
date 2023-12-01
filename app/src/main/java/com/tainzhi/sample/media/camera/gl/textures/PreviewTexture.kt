package com.tainzhi.sample.media.camera.gl.textures

import android.graphics.RectF
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.util.Log
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.gl.GlUtil
import com.tainzhi.sample.media.camera.gl.Shader
import com.tainzhi.sample.media.camera.gl.ShaderFactory
import com.tainzhi.sample.media.camera.gl.ShaderType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PreviewTexture(
) : Texture() {
    private var hTexturePosition = 0
    private lateinit var textureSize: Vertex2F
    private lateinit var textureMatrix: FloatArray
    private var isTrueAspectRatio: Int = 0
    private var textureId: Int = 0
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureVertexBuffer: FloatBuffer
    private var filterTextureId: Int = 0
    var filterType = 0

    //纹理坐标
    private var textureVertices = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    )

    override fun onSetShader(): Shader = shaderFactory.getShader(ShaderType.CAMERA_PREVIEW)

    override fun load(shaderFactory: ShaderFactory) {
        super.load(shaderFactory)
        hTexturePosition = GLES20.glGetAttribLocation(shader.programHandle, "a_TexturePosition")
        filterTextureId = GlUtil.loadTextureFromRes(R.drawable.amatorka)
        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVertexBuffer.put(textureVertices).position(0)
    }

    override fun unload() {
        Log.d(TAG, "unload: ")
        GlUtil.deleteTexture(textureId)
        GlUtil.deleteTexture(filterTextureId)
        super.unload()
    }

    override fun onDraw() {
        super.onDraw()
        setMat4("u_TextureMatrix", textureMatrix)
        setVec2("u_TextureSize", floatArrayOf(textureSize.x, textureSize.y))
        setInt("u_IsTrueAspectRatio", isTrueAspectRatio)
        // bind preview texture
        GLES20.glActiveTexture(PREVIEW_TEXTURE)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        setInt("u_TextureSampler", PREVIEW_TEXTURE - GLES20.GL_TEXTURE0)
        // bind filter texture
        GLES20.glActiveTexture(FILTER_TEXTURE)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, filterTextureId)
        setInt("u_textureLUT", FILTER_TEXTURE - GLES20.GL_TEXTURE0)
        setInt("u_filterType", filterType)
        // set vertex attribute
        GLES20.glVertexAttribPointer(programHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(programHandle)
        GLES20.glVertexAttribPointer(
            hTexturePosition,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            textureVertexBuffer
        )
        GLES20.glEnableVertexAttribArray(hTexturePosition)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(programHandle)
        GLES20.glDisableVertexAttribArray(hTexturePosition)
    }

    fun changeFilterType() {
        filterType =( filterType + 1) % 7
        Log.d(TAG, "changeFilterType: type=$filterType")
    }

    fun setLayout(
        textureId: Int,
        textureSize: Vertex2F,
        textureMatrix: FloatArray,
        isTrueAspectRatio: Int,
        previewRectF: RectF
    ) {
        Log.d(TAG, "setLayout: ")
        //顶点坐标
        // 忽略z维度，只保留x,y维度
        val vertices = floatArrayOf(
            previewRectF.left, previewRectF.top,
            previewRectF.left, previewRectF.bottom,
            previewRectF.right, previewRectF.top,
            previewRectF.right, previewRectF.bottom
        )

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        this.textureId = textureId
        this.textureSize = textureSize
        this.textureMatrix = textureMatrix
        this.isTrueAspectRatio = isTrueAspectRatio
    }

    companion object {
        private val TAG = PreviewTexture::class.java.simpleName
    }
}