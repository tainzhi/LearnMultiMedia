package com.tainzhi.sample.media.camera

import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.util.Size
import com.tainzhi.sample.media.camera.gl.BaseGLSL
import com.tainzhi.sample.media.camera.gl.filter.OesFilter
import com.tainzhi.sample.media.camera.gl.textures.LineTexture
import com.tainzhi.sample.media.camera.gl.textures.Vertex2F
import com.tainzhi.sample.media.camera.gl.textures.Vertex3F
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraPreviewRender : GLSurfaceView.Renderer {
    private val mOesFilter = OesFilter()
    private val line0 = LineTexture()
    private val line1 = LineTexture()
    private val line2 = LineTexture()
    private var windowWidth = 0
    private var windowHeight = 0
    private var textureWidth = 0
    private var textureHeight = 0
    private var previewRectF: RectF = RectF()
    private val matrix = FloatArray(16)
    private val textureMatrix = FloatArray(16)
    private lateinit var surfaceTexture: SurfaceTexture
    var surfaceTextureListener: CameraPreviewView.SurfaceTextureListener? = null
    private var isSurfaceTextureValid = false

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        // set up alpha blending and an android background color
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        BaseGLSL.checkGlError("glBlendFunc")
        mOesFilter.create()
        line0.create()
        line1.create()
        line2.create()

        mOesFilter.setVertices(
            Vertex2F(previewRectF.left, previewRectF.top),
            Vertex2F(previewRectF.left, previewRectF.bottom),
            Vertex2F(previewRectF.right, previewRectF.top),
            Vertex2F(previewRectF.right, previewRectF.bottom)
        )
        line0.setVertices(Vertex3F(previewRectF.left, previewRectF.top, 0f), Vertex3F(previewRectF.right, previewRectF.bottom, 0f))
        line1.setVertices(Vertex3F(previewRectF.left, previewRectF.bottom, 0f), Vertex3F(previewRectF.right, previewRectF.top, 0f))
        line2.setVertices(Vertex3F(previewRectF.left, (previewRectF.bottom + previewRectF.top)/2f, 0f), Vertex3F(previewRectF.right, (previewRectF.bottom + previewRectF.top)/2f, 0f))
        calculateMatrix()
        Log.d(TAG, "onSurfaceCreated: ${windowWidth}x${windowHeight}")
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: ${width}x${height}")
    }

    override fun onDrawFrame(gl: GL10) {
        surfaceTextureListener?.onSurfaceTextureUpdated(surfaceTexture)
        // set black background
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // reset blend
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_CONSTANT_ALPHA)
        surfaceTexture.updateTexImage()
        mOesFilter.draw()
        line0.draw()
        line1.draw()
        line2.draw()
        if (isSurfaceTextureValid) {
            surfaceTexture.updateTexImage()
        }
    }

    fun setWindowSize(windowSize: Size, rectF: RectF) {
        Log.d(TAG, "setWindowSize: w${windowSize.width}*h${windowSize.height}, previewViewSize:w${previewRectF.width()}*h${previewRectF.height()}")
        this.windowWidth = windowSize.width
        this.windowHeight = windowSize.height
        previewRectF = rectF
    }

    // camera output texture size, width > height
    fun setTextureSize(previewTextureSize: Size, isTrueAspectRatio: Boolean) {
        Log.d(TAG, "setTextureSize:w${previewTextureSize.width}*h${previewTextureSize.height}, trueAspectRatio:${isTrueAspectRatio}")
        this.textureWidth = previewTextureSize.width
        this.textureHeight = previewTextureSize.height
        mOesFilter.textureSize = Vertex2F(previewTextureSize.width.toFloat(), previewTextureSize.height.toFloat())
        mOesFilter.isTrueAspectRatio = if (isTrueAspectRatio) 1 else 0
        calculateMatrix()
    }

    fun releaseSurfaceTexture() {
        Log.d(TAG, "releaseSurfaceTexture: ")
        isSurfaceTextureValid = false
        surfaceTexture.release()
    }

    fun createSurfaceTexture(width: Int, height: Int) {
        Log.d(TAG, "createSurfaceTexture: w${width}*h${height}")
        // texture 不能在UI thread创建，只能在其他线程创建，比如 GLThread
        // 在 onSurfaceCreated回调就在 GLThread 被执行
        val texture = createTextureID()
        surfaceTexture = SurfaceTexture(texture)
        surfaceTexture.setDefaultBufferSize(width, height)
        surfaceTexture.setOnFrameAvailableListener{
            surfaceTextureListener?.onSurfaceTextureAvailable(surfaceTexture, width, height)
        }
        mOesFilter.textureId = texture
        surfaceTextureListener?.onSurfaceTextureCreated(surfaceTexture, width, height)
        isSurfaceTextureValid = true
    }

    private fun calculateMatrix() {
        getShowMatrix(matrix, windowWidth, windowHeight)
        getShowTextureMatrix(textureMatrix, textureWidth, textureHeight, windowWidth, windowHeight)
        mOesFilter.matrix = matrix
        (mOesFilter as OesFilter).setTextureMatrix(textureMatrix)
        line0.mvpMatrix = matrix
        line1.mvpMatrix = matrix
        line2.mvpMatrix = matrix
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

        // 坐标轴原点在 top left
        // positive x-axis points right
        // positive y-axis points bottom
        fun getShowMatrix(mvpMatrix: FloatArray?, viewWidth: Int, viewHeight: Int) {
            Log.d(TAG, "getShowMatrix: w${viewWidth}*h${viewHeight}")
            val modelMatrix = FloatArray(16)
            Matrix.setIdentityM(modelMatrix, 0)

            val viewMatrix = FloatArray(16)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)

            val projectionMatrix = FloatArray(16)
            // 使得positive y-axis points to bottom
            Matrix.orthoM(projectionMatrix, 0, 0f, viewWidth.toFloat(), viewHeight.toFloat(), 0f, 1f, 3f)

            Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
        }

        fun getShowTextureMatrix(mvpMatrix: FloatArray?, imgWidth: Int, imgHeight: Int, viewWidth: Int, viewHeight: Int) {
            Log.d(TAG, "getShowTextureMatrix: img:w${imgWidth}*h${imgHeight}, view:w${viewWidth}*h${viewHeight}")
            if (imgHeight > 0 && imgWidth > 0 && viewWidth > 0 && viewHeight > 0) {

                val modelMatrix = FloatArray(16)
                Matrix.setIdentityM(modelMatrix, 0)
                // 4. move center back to original
                Matrix.translateM(modelMatrix,0, 0.5f, 0.5f, 0f);
                // 3. rotate
                Matrix.rotateM(modelMatrix, 0, 90f, 0f, 0f, 1f)
                // 2. flip
                Matrix.scaleM(modelMatrix, 0, -1.0f, -1.0f, 1.0f)
                // 1. move to center (0, 0)
                Matrix.translateM(modelMatrix,0, -0.5f, -0.5f, 0f);
//                val scale: Float =  viewWidth.toFloat()
//                Matrix.scaleM(modelMatrix, 0, scale * (imgWidth / imgHeight.toFloat()), scale, 1f)
                val viewMatrix = FloatArray(16)
                Matrix.setIdentityM(viewMatrix, 0)
//                Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
                val projectionMatrix = FloatArray(16)
                Matrix.setIdentityM(projectionMatrix, 0)
//                Matrix.orthoM(projectionMatrix, 0, 0f, viewWidth.toFloat(), 0f, viewHeight.toFloat(),  1f, 3f)
                Matrix.multiplyMM(mvpMatrix, 0, viewMatrix, 0, modelMatrix, 0)
                Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, mvpMatrix, 0)
            }
        }
    }
}