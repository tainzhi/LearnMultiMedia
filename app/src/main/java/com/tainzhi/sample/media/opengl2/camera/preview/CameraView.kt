package com.tainzhi.sample.media.opengl2.camera.preview

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.renhui.opengles20study.camera.preview.GLCameraDrawer
import com.tainzhi.sample.media.opengl2.base.BaseGLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 摄像头展示界面
 */
class CameraView(context: Context) : BaseGLSurfaceView(context), GLSurfaceView.Renderer {
    private var mCamera: GLCamera
    private var mCameraDrawer: GLCameraDrawer
    
    constructor(context: Context, attr: AttributeSet) : this(context)
    
    init {
        setRenderer(this) // 设置渲染器
        mCamera = GLCamera() // 创建摄像头资源
        mCameraDrawer = GLCameraDrawer()
    }
    
    // 切换摄像头
    fun switchCamera() {
        cameraId = if (cameraId == 1) 0 else 1
        onPause()
        onResume()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mCameraDrawer.onSurfaceCreated(gl, config)
        mCamera.open(cameraId) // 打开指定的摄像头
        val point = mCamera.previewSize
        mCameraDrawer.setDataSize(point!!.x, point.y)
        mCamera.setPreviewTexture(mCameraDrawer.surfaceTexture) // 将SurfaceTexture和摄像头预览绑定
        mCameraDrawer.surfaceTexture?.setOnFrameAvailableListener { requestRender() }
        mCamera.preview()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        mCameraDrawer.setViewSize(width, height)
    }

    override fun onDrawFrame(gl: GL10) {
        mCameraDrawer.onDrawFrame(gl)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        mCamera.close()
    }

    companion object {
        var cameraId = 1 // 要打开的摄像头的ID
    }
}