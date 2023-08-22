package com.tainzhi.sample.media.camera

import android.content.Context
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import com.tainzhi.sample.media.CamApp
import com.tainzhi.sample.media.camera.gl.EglUtil
import com.tainzhi.sample.media.camera.gl.textures.TextureManager
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

class CameraPreviewView : GLSurfaceView {

    lateinit var render: CameraPreviewRender
    private var egl10: EGL10? = null
    private var eglDisplay = EGL10.EGL_NO_DISPLAY
    private var eglSurface = EGL10.EGL_NO_SURFACE
    private var eglContext = EGL10.EGL_NO_CONTEXT

    private val textureManager = TextureManager()
    constructor(context: Context): super(context) {}

    constructor(context: Context, attr: AttributeSet) : super(context, attr) {
        setEGLContextFactory(ContextFactory())
        setEGLWindowSurfaceFactory(WindowSurfaceFactory())
        setEGLConfigChooser(8, 8, 8, 8, 16, 8)
        setZOrderMediaOverlay(true)
        setEGLContextClientVersion(3)
    }

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

    fun setTextureSize(previewTextureSize: Size, isTrueAspectRatio: Boolean, rectF: RectF, isFrontCamera: Boolean) {
        render.setTextureSize(previewTextureSize, isTrueAspectRatio,rectF, isFrontCamera)
        createSurface(previewTextureSize.width, previewTextureSize.height)
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
            if (CamApp.Debug) Log.d(TAG, "createWindowSurface: ")
            egl10 = egl
            eglDisplay = display
            eglSurface = createSurfaceImpl(egl!!, display!!, config!!, nativeWindow!!)
            EglUtil.makeCurrent(egl, display, eglSurface, eglContext)
            return eglSurface
        }

        override fun destroySurface(egl: EGL10?, display: EGLDisplay?, surface: EGLSurface?) {
            if (CamApp.Debug) Log.d(TAG, "destroySurface: ")
            if (!EglUtil.destroySurface(egl, display, surface)) {
                Log.w(TAG, "failed to destroy OpenGL ES surface")
            }
            eglSurface = EGL10.EGL_NO_SURFACE
            eglDisplay = EGL10.EGL_NO_DISPLAY
            egl10 = null
        }

        private fun createSurfaceImpl(egl: EGL10, display: EGLDisplay, eglConfig: EGLConfig, nativeWindow: Any): EGLSurface {
            if (CamApp.Debug) Log.d(TAG, "create  OpenGL ES surface")
            try {
                val surface = EglUtil.createWindowSurface(egl, display, eglConfig, nativeWindow)
                if (surface != EGL10.EGL_NO_SURFACE) return surface
                Log.w(TAG, "failed to create OpenGL ES surface")
            } catch (e: RuntimeException) {
                Log.w(TAG, "failed to create OpenGL ES surface, ${e.message}")
            }
            return EGL10.EGL_NO_SURFACE
        }
    }

    inner class ContextFactory: EGLContextFactory {

        override fun createContext(egl: EGL10?, display: EGLDisplay?, eglConfig: EGLConfig?): EGLContext {
            if (CamApp.Debug) Log.d(TAG, "createContext: ")
            eglContext = createContextImpl(egl!!, display!!, eglConfig!!)
            textureManager.loadTextures()
            return eglContext
        }

        override fun destroyContext(egl: EGL10?, display: EGLDisplay?, context: EGLContext?) {
            if (CamApp.Debug) Log.d(TAG, "destroyContext: ")
            textureManager.unloadTextures()
            if (!EglUtil.destroyContext(egl!!, display!!, context!!)) {
                Log.w(TAG, "failed to destroy OpenGL ES context")
            }
            eglContext = EGL10.EGL_NO_CONTEXT
        }

        private fun createContextImpl(egl: EGL10, display: EGLDisplay, eglConfig: EGLConfig): EGLContext {
            val versions = intArrayOf(EglUtil.GLES3, EglUtil.GLES2)
            versions.forEach {
                if (CamApp.Debug) {
                    Log.d(TAG, "create OpenGL ES context with version:${it}")
                }
                try {
                    val context = EglUtil.createContext(egl, display, eglConfig, it)
                    if (context != EGL10.EGL_NO_CONTEXT) {
                        return context
                    }
                    Log.w(TAG, "failed to create OpenGL ES context with version:$it")
                } catch (e: RuntimeException) {
                    Log.w(TAG, "failed to create OpenGL ES context with version:$it, ${e.message}")
                }
            }
            return EGL10.EGL_NO_CONTEXT
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