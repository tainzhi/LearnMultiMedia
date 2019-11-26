package com.tainzhi.sample.media.opengl2.camera.filter

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import com.tainzhi.sample.media.opengl2.camera.takepic.utils.EasyGlUtils

class TextureFilter : BaseFilter() {
    private val mFilter: CameraFilter
    private var width = 0
    private var height = 0
    private val fFrame = IntArray(1)
    private val fTexture = IntArray(1)
    private val mCameraTexture = IntArray(1)
    var texture: SurfaceTexture? = null
        private set
    private val mCoordOM = FloatArray(16)
    fun setCoordMatrix(matrix: FloatArray?) {
        mFilter.setCoordMatrix(matrix!!)
    }

    override var flag: Int
        get() = super.flag
        set(flag) {
            mFilter.flag = flag
        }

    override fun initBuffer() {}
    override var matrix: FloatArray
        get() = super.matrix
        set(matrix) {
            mFilter.matrix = matrix
        }

    override val outputTexture: Int
        get() = fTexture[0]

    override fun draw() {
        val a = GLES20.glIsEnabled(GLES20.GL_DEPTH_TEST)
        if (a) {
            GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        }
        if (texture != null) {
            texture!!.updateTexImage()
            texture!!.getTransformMatrix(mCoordOM)
            mFilter.setCoordMatrix(mCoordOM)
        }
        EasyGlUtils.bindFrameTexture(fFrame[0], fTexture[0])
        GLES20.glViewport(0, 0, width, height)
        mFilter.textureId = mCameraTexture[0]
        mFilter.draw()
        EasyGlUtils.unBindFrameBuffer()
        if (a) {
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        }
    }

    override fun onCreate() {
        mFilter.create()
        createOesTexture()
        texture = SurfaceTexture(mCameraTexture[0])
    }

    override fun onSizeChanged(width: Int, height: Int) {
        mFilter.setSize(width, height)
        if (this.width != width || this.height != height) {
            this.width = width
            this.height = height
            //创建FrameBuffer和Texture
            deleteFrameBuffer()
            GLES20.glGenFramebuffers(1, fFrame, 0)
            EasyGlUtils.genTexturesWithParameter(1, fTexture, 0, GLES20.GL_RGBA, width, height)
        }
    }

    private fun deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, fFrame, 0)
        GLES20.glDeleteTextures(1, fTexture, 0)
    }

    private fun createOesTexture() {
        GLES20.glGenTextures(1, mCameraTexture, 0)
    }

    init {
        mFilter = CameraFilter()
    }
}