package com.tainzhi.sample.media.camera

import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.tainzhi.sample.media.opengl2.camera.filter.BaseFilter
import com.tainzhi.sample.media.opengl2.camera.filter.OesFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraPreviewRender : GLSurfaceView.Renderer {
    private lateinit var surfaceTexture: SurfaceTexture
    private val mOesFilter: BaseFilter = OesFilter()
    private var width = 0
    private var height = 0
    private var dataWidth = 0
    private var dataHeight = 0
    private val matrix = FloatArray(16)
    var surfaceTextureListener: CameraPreviewView.SurfaceTextureListener? = null

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated: ${width}x${height}")
        // texture 不能在UI thread创建，只能在其他线程创建，比如 GLThread
        // 在 onSurfaceCreated回调就在 GLThread 被执行
        val texture = createTextureID()
        surfaceTexture = SurfaceTexture(texture)
        surfaceTexture.setDefaultBufferSize(2400, 1080)
        surfaceTexture.setOnFrameAvailableListener{
            surfaceTextureListener?.onSurfaceTextureAvailable(surfaceTexture, width, height)
        }
        mOesFilter.create()
        mOesFilter.textureId = texture
        surfaceTextureListener?.onSurfaceTextureCreated(surfaceTexture, width, height)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        surfaceTextureListener?.onSurfaceTextureSizeChanged(surfaceTexture, width, height)
        // 在全屏模式下，onSurfaceCreated和onSurfaceChanged的宽高不一样，需要重新设置输出预览大小
        surfaceTexture.setDefaultBufferSize(height, width)
        setViewSize(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        surfaceTextureListener?.onSurfaceTextureUpdated(surfaceTexture)
        surfaceTexture.updateTexImage()
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
        // flip(matrix, true, false)
        mOesFilter.matrix = matrix
    }

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
        private val TAG = CameraPreviewRender::class.java.simpleName

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

        fun getShowMatrix(mvpMatrix: FloatArray?, imgWidth: Int, imgHeight: Int, viewWidth: Int, viewHeight: Int) {
            Log.d(TAG, "getShowMatrix: img:w${imgWidth}*h${imgHeight}, view:w${viewWidth}*h${viewHeight}")
            if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {
                val sWhView = viewWidth.toFloat() / viewHeight
                val sWhImg = imgWidth.toFloat() / imgHeight

                val modelMatrix = FloatArray(16)
                Matrix.setIdentityM(modelMatrix, 0)
                Matrix.rotateM(modelMatrix, 0, 270f, 0f, 0f, 1f)
                val scale: Float =  viewWidth/2f
                Matrix.scaleM(modelMatrix, 0, scale * (imgWidth / imgHeight.toFloat()), scale, 1f)
                val viewMatrix = FloatArray(16)
                Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f)
                val projectionMatrix = FloatArray(16)
                Matrix.orthoM(projectionMatrix, 0, -viewWidth/2f, viewWidth/2f, -viewHeight/2f, viewHeight/2f, 1f, 3f)
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
            }
        }
    }
}