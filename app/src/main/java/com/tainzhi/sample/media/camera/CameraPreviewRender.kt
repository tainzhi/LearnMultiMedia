package com.tainzhi.sample.media.camera

import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.util.Size
import com.tainzhi.sample.media.camera.gl.textures.PreviewTexture
import com.tainzhi.sample.media.camera.gl.textures.TextureManager
import com.tainzhi.sample.media.camera.gl.textures.Vertex2F
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class CameraPreviewRender : GLSurfaceView.Renderer {
    private val previewTexture = PreviewTexture()
    private var windowWidth = 0
    private var windowHeight = 0
    private var textureWidth = 0
    private var textureHeight = 0
    private var previewRectF: RectF = RectF()
    private val modelMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val textureMatrix = FloatArray(16)
    private var surfaceTexture: SurfaceTexture? = null
    var surfaceTextureListener: CameraPreviewView.SurfaceTextureListener? = null
    private var isFrontCamera = false
    lateinit var textureManager: TextureManager

    // invoked when EglContext created
    // not need to invoke surfaceTextureListener?.onSurfaceTextureCreated
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated: ${windowWidth}x${windowHeight}")
    }

    // first invoked after EglContext created
    // and get the width*height of FullScreen GlSurfaceView and transport to CameraActivity
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged: ${width}x${height}")
        windowWidth = width
        windowHeight = height
        surfaceTextureListener?.onSurfaceTextureChanged(surfaceTexture, width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        if (surfaceTexture != null) {
            surfaceTextureListener?.onSurfaceTextureUpdated(surfaceTexture)
            // set black background
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            // reset blend
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_CONSTANT_ALPHA)
            previewTexture.draw()
            surfaceTexture?.updateTexImage()
        }
        textureManager.onDraw()
    }

    // camera output texture size, width > height
    fun setTextureSize(
        previewTextureSize: Size,
        isTrueAspectRatio: Boolean,
        rectF: RectF,
        isFrontCamera: Boolean
    ) {
        Log.d(
            TAG,
            "setTextureSize:w${previewTextureSize.width}*h${previewTextureSize.height}, trueAspectRatio:${isTrueAspectRatio}"
        )
        this.textureWidth = previewTextureSize.width
        this.textureHeight = previewTextureSize.height
        this.isFrontCamera = isFrontCamera
        this.previewRectF = rectF
        textureManager.previewRectF = previewRectF
        previewTexture.textureSize =
            Vertex2F(previewTextureSize.width.toFloat(), previewTextureSize.height.toFloat())
        previewTexture.isTrueAspectRatio = if (isTrueAspectRatio) 1 else 0
        calculateMatrix()
        setMatrixToShader()
    }

    fun releaseSurfaceTexture() {
        Log.d(TAG, "releaseSurfaceTexture: ")
        surfaceTexture?.release()
        surfaceTexture = null
    }

    fun createSurfaceTexture(width: Int, height: Int) {
        Log.d(TAG, "createSurfaceTexture: w${width}*h${height}")
        // texture 不能在UI thread创建，只能在其他线程创建，比如 GLThread
        // 在 onSurfaceCreated回调就在 GLThread 被执行
        val texture = createTextureID()
        surfaceTexture = SurfaceTexture(texture).apply {
            setDefaultBufferSize(width, height)
            setOnFrameAvailableListener {
                surfaceTextureListener?.onSurfaceTextureAvailable(surfaceTexture, width, height)
            }
        }

        // set up alpha blending and an android background color
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        previewTexture.run {
            create()
            textureId = texture
            setVertices(
                Vertex2F(previewRectF.left, previewRectF.top),
                Vertex2F(previewRectF.left, previewRectF.bottom),
                Vertex2F(previewRectF.right, previewRectF.top),
                Vertex2F(previewRectF.right, previewRectF.bottom)
            )
        }

        surfaceTextureListener?.onSurfaceTextureCreated(surfaceTexture, width, height)
    }

    private fun calculateMatrix() {
        // 坐标轴原点在 top left
        // positive x-axis points right
        // positive y-axis points bottom
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1f, 0f)
        // 使得positive y-axis points to bottom
        Matrix.orthoM(
            projectionMatrix,
            0,
            0f,
            windowWidth.toFloat(),
            windowHeight.toFloat(),
            0f,
            1f,
            3f
        )

        Matrix.setIdentityM(textureMatrix, 0)
        // 4. move center back to original
        Matrix.translateM(textureMatrix, 0, 0.5f, 0.5f, 0f)
        // 3. rotate
        Matrix.rotateM(textureMatrix, 0, if (isFrontCamera) 270f else 90f, 0f, 0f, 1f)
        // 2. flip
        Matrix.scaleM(textureMatrix, 0, if (isFrontCamera) 1.0f else -1.0f, -1.0f, 1.0f)
        // 1. move to center (0, 0)
        Matrix.translateM(textureMatrix, 0, -0.5f, -0.5f, 0f)
    }

    private fun setMatrixToShader() {
        previewTexture.setMatrix(modelMatrix, viewMatrix, projectionMatrix, textureMatrix)
        textureManager.setMatrix(modelMatrix, viewMatrix, projectionMatrix)
    }

    private fun createTextureID(): Int {
        val texture = IntArray(1)
        GLES20.glGenTextures(1, texture, 0) // 创建纹理，生成你要操作的纹理对象的索引
        GLES20.glBindTexture(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            texture[0]
        ) // 绑定纹理，告诉OpenGL下面代码中对2D纹理的任何设置都是针对索引为0的纹理的
        // glTexParameterf 是设置纹理贴图的参数属性
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MIN_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_MAG_FILTER,
            GL10.GL_LINEAR.toFloat()
        )
        // OpenGL——纹理过滤函数glTexParameteri() 确定如何把纹理象素映射成像素.
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_S,
            GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GL10.GL_TEXTURE_WRAP_T,
            GL10.GL_CLAMP_TO_EDGE
        )
        return texture[0]
    }

    companion object {
        private val TAG = CameraPreviewRender::class.java.simpleName
    }
}