package com.renhui.opengles20study.camera.preview

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.tainzhi.sample.media.opengl2.camera.filter.BaseFilter
import com.tainzhi.sample.media.opengl2.camera.filter.OesFilter
import com.tainzhi.sample.media.opengl2.camera.preview.CameraView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL 摄像头绘制器
 */
class GLCameraDrawer : GLSurfaceView.Renderer {
    var surfaceTexture: SurfaceTexture? = null
        private set
    private val mOesFilter: BaseFilter
    private var width = 0
    private var height = 0
    private var dataWidth = 0
    private var dataHeight = 0
    private val matrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        val texture = createTextureID()
        surfaceTexture = SurfaceTexture(texture)
        mOesFilter.create()
        mOesFilter.textureId = texture
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        setViewSize(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        if (surfaceTexture != null) { // 更新纹理图像为从图像流中提取的最近一帧
            surfaceTexture!!.updateTexImage()
        }
        // 绘制纹理
        mOesFilter.draw()
    }

    fun setViewSize(width: Int, height: Int) {
        this.width = width
        this.height = height
        calculateMatrix()
    }

    fun setDataSize(dataWidth: Int, dataHeight: Int) {
        this.dataWidth = dataWidth
        this.dataHeight = dataHeight
        calculateMatrix()
    }

    private fun calculateMatrix() {
        getShowMatrix(matrix, dataWidth, dataHeight, width, height)
        if (CameraView.Companion.cameraId == 1) {
            flip(matrix, true, false)
            rotate(matrix, 90f)
        } else {
            rotate(matrix, 270f)
        }
        mOesFilter.matrix = matrix
    }

    // 创建纹理ID
    private fun createTextureID(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0) // 创建纹理，生成你要操作的纹理对象的索引
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]) // 绑定纹理，告诉OpenGL下面代码中对2D纹理的任何设置都是针对索引为0的纹理的
        // glTexParameterf 是设置纹理贴图的参数属性
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        // OpenGL——纹理过滤函数glTexParameteri() 确定如何把纹理象素映射成像素.
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        return texture[0]
    }

    companion object {
        fun rotate(m: FloatArray, angle: Float): FloatArray {
            Matrix.rotateM(m, 0, angle, 0f, 0f, 1f)
            return m
        }

        fun flip(m: FloatArray, x: Boolean, y: Boolean): FloatArray {
            if (x || y) {
                Matrix.scaleM(m, 0, if (x) (-1).toFloat() else 1.toFloat(), if (y) (-1).toFloat() else 1.toFloat(), 1f)
            }
            return m
        }

        fun getShowMatrix(matrix: FloatArray?, imgWidth: Int, imgHeight: Int, viewWidth: Int, viewHeight: Int) {
            if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
                val sWhView = viewWidth.toFloat() / viewHeight
                val sWhImg = imgWidth.toFloat() / imgHeight
                val projection = FloatArray(16)
                val camera = FloatArray(16)
                if (sWhImg > sWhView) {
                    Matrix.orthoM(projection, 0, -sWhView / sWhImg, sWhView / sWhImg, -1f, 1f, 1f, 3f)
                } else {
                    Matrix.orthoM(projection, 0, -1f, 1f, -sWhImg / sWhView, sWhImg / sWhView, 1f, 3f)
                }
                Matrix.setLookAtM(camera, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                Matrix.multiplyMM(matrix, 0, projection, 0, camera, 0)
            }
        }
    }

    init {
        mOesFilter = OesFilter()
    }
}