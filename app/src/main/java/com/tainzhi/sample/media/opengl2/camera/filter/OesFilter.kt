package com.tainzhi.sample.media.opengl2.camera.filter

import android.opengl.GLES11Ext
import android.opengl.GLES20
import java.util.*

open class OesFilter : BaseFilter() {
    var vertexShaderCode = "attribute vec4 vPosition;\n" +
            "attribute vec2 vCoord;\n" +
            "uniform mat4 vMatrix;\n" +
            "uniform mat4 vCoordMatrix;\n" +
            "varying vec2 textureCoordinate;\n" +
            "void main(){\n" +
            "    gl_Position = vMatrix*vPosition;\n" +
            "    textureCoordinate = (vCoordMatrix*vec4(vCoord,0,1)).xy;\n" +
            "}"
    var fragmentShaderCode = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "varying vec2 textureCoordinate;\n" +
            "uniform samplerExternalOES vTexture;\n" +
            "void main() {\n" +
            "    gl_FragColor = texture2D( vTexture, textureCoordinate );\n" +
            "}"
    private var mHCoordMatrix = 0
    private var mCoordMatrix: FloatArray = Arrays.copyOf(BaseFilter.Companion.OM, 16)
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