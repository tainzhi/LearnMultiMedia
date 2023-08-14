package com.tainzhi.sample.media.camera.gl.filter

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.tainzhi.sample.media.R
import java.util.Arrays

open class OesFilter : BaseFilter() {
    var vertexShaderCode = getShaderSource(R.raw.preview_vs)
    var fragmentShaderCode = getShaderSource(R.raw.preview_fs)
    private var mHTextureMatrix = 0
    private var mTextureMatrix: FloatArray = Arrays.copyOf(OM, 16)
    override fun onCreate() {
        createProgram(vertexShaderCode, fragmentShaderCode)
        mHTextureMatrix = GLES20.glGetUniformLocation(mProgram, "u_TextureMatrix")
    }

    fun setCoordMatrix(matrix: FloatArray) {
        mTextureMatrix = matrix
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniformMatrix4fv(mHTextureMatrix, 1, false, mTextureMatrix, 0)
    }

    override fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(mHTexture, textureType)
    }

    override fun onSizeChanged(width: Int, height: Int) {}
}