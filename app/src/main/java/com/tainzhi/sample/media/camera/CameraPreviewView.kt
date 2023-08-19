package com.tainzhi.sample.media.camera

import android.content.Context
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.util.Size
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

    lateinit var render: CameraPreviewRender

    var surfaceTextureListener: SurfaceTextureListener? = null
        set(value) {
            render.surfaceTextureListener = value
        }

    fun setRender(render: GLSurfaceView.Renderer) {
        Log.d(TAG, "setRender: ")
        setRenderer(render)
        this.render = render as CameraPreviewRender
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    fun setTextureSize(previewTextureSize: Size, isTrueAspectRatio: Boolean) {
        render.setTextureSize(previewTextureSize, isTrueAspectRatio)
        createSurface(previewTextureSize.width, previewTextureSize.height)
    }

    fun setWindowSize(windowSize: Size, rectF: RectF, isFrontCamera: Boolean) {
        render.setWindowSize(windowSize, rectF, isFrontCamera)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    fun createSurface(width: Int, height: Int) {
        queueEvent {
            render.createSurfaceTexture(width, height)
        }
    }

    fun releaseSurface() {
        queueEvent {
            render.releaseSurfaceTexture()
        }
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
        fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int)
        fun onSurfaceTextureCreated(surface: SurfaceTexture?, width: Int, height: Int)
        fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean
        fun onSurfaceTextureUpdated(surface: SurfaceTexture?)

        fun onSurfaceTextureChanged(surfaceTexture: SurfaceTexture?, width: Int, height: Int)
    }

    companion object {
        private val TAG = CameraPreviewView::class.java.simpleName
    }
}