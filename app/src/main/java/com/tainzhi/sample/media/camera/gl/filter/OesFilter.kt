package com.tainzhi.sample.media.camera.gl.filter

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.tainzhi.sample.media.R
import java.util.Arrays

open class OesFilter : BaseFilter() {
    var vertexShaderCode = getShaderSource(R.raw.preview_vs)
    var fragmentShaderCode = getShaderSource(R.raw.preview_fs)
    private var mHCoordMatrix = 0
    private var mCoordMatrix: FloatArray = Arrays.copyOf(OM, 16)
    override fun onCreate() {
        createProgram(vertexShaderCode, fragmentShaderCode)
        mHCoordMatrix = GLES20.glGetUniformLocation(mProgram, "vCoordMatrix")
    }

    fun setCoordMatrix(matrix: FloatArray) {
        mCoordMatrix = matrix
    }

    override fun onSetExpandData() {
        super.onSetExpandData()
        GLES20.glUniformMatrix4fv(mHCoordMatrix, 1, false, mCoordMatrix, 0)
    }

    override fun onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        GLES20.glUniform1i(mHTexture, textureType)
    }

    override fun onSizeChanged(width: Int, height: Int) {}
}