package com.tainzhi.sample.media.camera.gl.textures

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.gl.GlUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class PreviewTexture : Texture(){
    protected var mHTexturePosition = 0
    private var mTextureMatrix: FloatArray = FloatArray(16)
    var textureSize = Vertex2F(0f, 0f)
    var isTrueAspectRatio = 0
    //顶点坐标
    // 忽略z维度，只保留x,y维度
    private var vertices = floatArrayOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)
    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var textureVertexBuffer: FloatBuffer
    var textureType = 0 //默认使用Texture2D0
    var textureId = 0
    //纹理坐标
    private var textureVertices = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 0.0f,
        1.0f, 1.0f)

    override fun onCreate() {
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        textureVertexBuffer = ByteBuffer.allocateDirect(textureVertices.size * 4)
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureVertexBuffer.put(textureVertices).position(0)

        createProgram(GlUtil.getShaderSource(R.raw.preview_vs), GlUtil.getShaderSource(R.raw.preview_fs))

        mHTexturePosition = GLES20.glGetAttribLocation(mProgram, "a_TexturePosition")
        GlUtil.checkGlError("get texture postion")
    }

    override fun onDraw() {
        super.onDraw()
        setMat4("u_TextureMatrix", mTextureMatrix)
        setVec2("u_TextureSize", floatArrayOf(textureSize.x, textureSize.y))
        setInt("u_IsTrueAspectRatio", isTrueAspectRatio)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        setInt("u_TextureSampler", textureType)

        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(mHTexturePosition)
        GLES20.glVertexAttribPointer(mHTexturePosition, 2, GLES20.GL_FLOAT, false, 0, textureVertexBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mHPosition)
        GLES20.glDisableVertexAttribArray(mHTexturePosition)
    }

    fun setVertices(topLeft: Vertex2F, bottomLeft: Vertex2F, topRight: Vertex2F, bottomRight:Vertex2F) {
//        vertices = floatArrayOf(
//            0f, 0f,
//            0f, height,
//            width, 0f,
//            width, height
//        )
        vertices = floatArrayOf(
            topLeft.x, topLeft.y,
            bottomLeft.x, bottomLeft.y,
            topRight.x, topRight.y,
            bottomRight.x, bottomRight.y
        )
        vertexBuffer.put(vertices).position(0)
    }

    fun setMatrix(model: FloatArray, view: FloatArray, projection:FloatArray, texMatrix: FloatArray) {
        modelMatrix = model
        viewMatrix = view
        projectionMatrix = projection
        mTextureMatrix = texMatrix
    }
}