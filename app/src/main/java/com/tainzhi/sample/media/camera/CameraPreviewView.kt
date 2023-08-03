package com.tainzhi.sample.media.camera

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

class CameraPreviewView : GLSurfaceView {
    constructor(context: Context): super(context) {}

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        // setEGLContextFactory(ContextFactory())
        // setEGLWindowSurfaceFactory(WindowSurfaceFactory())
        setEGLConfigChooser(8, 8, 8, 8, 16, 8)
        setZOrderMediaOverlay(true)
        setEGLContextClientVersion(3)
    }

    lateinit var cameraDrawer: CameraPreviewRender

    var surfaceTextureListener: SurfaceTextureListener? = null
        set(value) {
            cameraDrawer.surfaceTextureListener = value
        }

    fun setRender(render: GLSurfaceView.Renderer) {
        Log.d(TAG, "setRender: ")
        setRenderer(render)
        cameraDrawer = render as CameraPreviewRender
        renderMode = RENDERMODE_WHEN_DIRTY

        val display = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) context.display else {
            (context as Activity).windowManager.defaultDisplay
        }
        val displaySize = Point()
        display?.getSize(displaySize)
        cameraDrawer.setViewSize(displaySize.x, displaySize.y)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    inner class WindowSurfaceFactory: EGLWindowSurfaceFactory {

        override fun createWindowSurface(
            egl: EGL10?,
            display: EGLDisplay?,
            config: EGLConfig?,
            nativeWindow: Any?
        ): EGLSurface {
            TODO("Not yet implemented")
        }

        override fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?) {
            TODO("Not yet implemented")
        }
    }

    inner class ContextFactory: EGLContextFactory {

        override fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext {
            TODO("Not yet implemented")
        }

        override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
            TODO("Not yet implemented")
        }
    }

    interface SurfaceTextureListener {
        fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int)
        fun onSurfaceTextureCreated(surface: SurfaceTexture, width: Int, height: Int)
        fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int)
        fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean
        fun onSurfaceTextureUpdated(surface: SurfaceTexture)
    }

    companion object {
        private val TAG = CameraPreviewView::class.java.simpleName
    }
}