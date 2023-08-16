package com.tainzhi.sample.media.camera.gl.filter

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.gl.textures.Vertex2F
import java.util.Arrays

open class OesFilter : BaseFilter() {
    private var vertexShaderCode = getShaderSource(R.raw.preview_vs)
    private var fragmentShaderCode = getShaderSource(R.raw.preview_fs)
    private var mHTextureMatrix = 0
    private var mHTextureSize = 0
    private var mHIsTrueAspectRatio = 0
    private var mTextureMatrix: FloatArray = Arrays.copyOf(OM, 16)
    var textureSize = Vertex2F(0f, 0f)
    var isTrueAspectRatio = 0
    override fun onCreate() {
        createProgram(vertexShaderCode, fragmentShaderCode)
        mHTextureSize = GLES20.glGetUniformLocation(mProgram, "u_TextureSize")
        mHTextureMatrix = GLES20.glGetUniformLocation(mProgram, "u_TextureMatrix")
        mHIsTrueAspectRatio = GLES20.glGetUniformLocation(mProgram, "u_IsTrueAspectRatio")
    }

    override fun onSizeChanged(width: Int, height: Int) {
    }

    fun setMatrix(model: FloatArray, view: FloatArray, projection:FloatArray, texMatrix: FloatArray) {
        modelMatrix = model
        viewMatrix = view
        projectionMatrix = projection
        mTextureMatrix = texMatrix
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniformMatrix4fv(mHTextureMatrix, 1, false, mTextureMatrix, 0)
        GLES20.glUniform2fv(mHTextureSize, 1, floatArrayOf(textureSize.x, textureSize.y), 0)
        GLES20.glUniform1i(mHIsTrueAspectRatio, isTrueAspectRatio);

    }

    override fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(mHTexture, textureType)
    }
}