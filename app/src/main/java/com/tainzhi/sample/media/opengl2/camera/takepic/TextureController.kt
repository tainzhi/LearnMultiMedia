package com.tainzhi.sample.media.opengl2.camera.takepic

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import com.tainzhi.sample.media.opengl2.camera.filter.BaseFilter
import com.tainzhi.sample.media.opengl2.camera.filter.GroupFilter
import com.tainzhi.sample.media.opengl2.camera.filter.NoFilter
import com.tainzhi.sample.media.opengl2.camera.filter.TextureFilter
import com.tainzhi.sample.media.opengl2.camera.takepic.utils.EasyGlUtils
import com.tainzhi.sample.media.opengl2.camera.takepic.utils.MatrixUtils
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10

/**
 * Description: 借助GLSurfaceView创建的GL环境，做渲染工作。不将内容渲染到GLSurfaceView
 * 的Surface上，而是将内容绘制到外部提供的Surface、SurfaceHolder或者SurfaceTexture上。
 */
class TextureController(private val mContext: Context, val holder: SurfaceHolder) : GLSurfaceView.Renderer {
    private var surface = holder.surface
    private var mGLView: GLView? = null
    private var mRenderer //用户附加的Renderer或用来监听Renderer
            : Renderer? = null
    private var mEffectFilter //特效处理的Filter
            : TextureFilter? = null
    private var mGroupFilter //中间特效
            : GroupFilter? = null
    private var mShowFilter //用来渲染输出的Filter
            : BaseFilter? = null
    private var mDataSize //数据的大小
            : Point? = null
    private var mWindowSize //输出视图的大小
            : Point? = null
    private val isParamSet = AtomicBoolean(false)
    private val SM = FloatArray(16) //用于绘制到屏幕上的变换矩阵
    private val mShowType = MatrixUtils.TYPE_CENTERCROP //输出到屏幕上的方式
    private var mDirectionFlag = -1 //AiyaFilter方向flag
    private val callbackOM = FloatArray(16) //用于绘制回调缩放的矩阵
    //创建离屏buffer，用于最后导出数据
    private val mExportFrame = IntArray(1)
    private val mExportTexture = IntArray(1)
    private var isShoot = false //一次拍摄flag
    private var outPutBuffer: Array<ByteBuffer?>? = arrayOfNulls(3) //用于存储回调数据的buffer
    private var mFrameCallback //回调
            : FrameCallback? = null
    private var frameCallbackWidth = 0
    private var frameCallbackHeight = 0 //回调数据的宽高 = 0
    private var indexOutput = 0 //回调数据使用的buffer索引
    fun surfaceCreated(nativeWindow: Any?) {
        mGLView!!.surfaceCreated(holder)
    }

    fun surfaceChanged(width: Int, height: Int) {
        mWindowSize!!.x = width
        mWindowSize!!.y = height
        mGLView!!.surfaceChanged(holder, PixelFormat.RGBA_8888, width, height)
    }

    fun surfaceDestroyed() {
        mGLView!!.surfaceDestroyed(holder)
    }

    //在Surface创建前，应该被调用
    fun setDataSize(width: Int?, height: Int?) {
        mDataSize!!.x = width ?: -1
        mDataSize!!.y = height ?: -1
    }

    val texture: SurfaceTexture?
        get() = mEffectFilter?.texture

    fun setImageDirection(flag: Int) {
        mDirectionFlag = flag
    }

    fun setRenderer(renderer: Renderer?) {
        mRenderer = renderer
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        mEffectFilter!!.create()
        mShowFilter!!.create()
        mGroupFilter!!.create()
        if (!isParamSet.get()) {
            if (mRenderer != null) {
                mRenderer!!.onSurfaceCreated(gl, config)
            }
            sdkParamSet()
        }
        calculateCallbackOM()
        mEffectFilter?.flag = mDirectionFlag
        deleteFrameBuffer()
        GLES20.glGenFramebuffers(1, mExportFrame, 0)
        EasyGlUtils.genTexturesWithParameter(1, mExportTexture, 0, GLES20.GL_RGBA, mDataSize!!.x, mDataSize!!.y)
    }

    private fun deleteFrameBuffer() {
        GLES20.glDeleteFramebuffers(1, mExportFrame, 0)
        GLES20.glDeleteTextures(1, mExportTexture, 0)
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        MatrixUtils.getMatrix(SM, mShowType, mDataSize!!.x, mDataSize!!.y, width, height)
        mShowFilter!!.setSize(width, height)
        mShowFilter?.matrix = SM
        mEffectFilter!!.setSize(mDataSize!!.x, mDataSize!!.y)
        mGroupFilter!!.setSize(mDataSize!!.x, mDataSize!!.y)
        mShowFilter!!.setSize(mDataSize!!.x, mDataSize!!.y)
        if (mRenderer != null) {
            mRenderer!!.onSurfaceChanged(gl, width, height)
        }
    }

    override fun onDrawFrame(gl: GL10) {
        if (isParamSet.get()) {
            mEffectFilter!!.draw() // 特效的过滤器先画
            mGroupFilter?.textureId = mEffectFilter?.outputTexture ?: -1
            // 其他过滤器拿到当前的SurfaceTexture再画
            mGroupFilter!!.draw()
            //显示传入的texture上，一般是显示在屏幕上
            GLES20.glViewport(0, 0, mWindowSize!!.x, mWindowSize!!.y)
            mShowFilter?.matrix = SM
            mShowFilter?.textureId = mGroupFilter?.outputTexture ?: -1
            mShowFilter!!.draw() // 其他过滤器拿到当前的SurfaceTexture再画
            if (mRenderer != null) {
                mRenderer!!.onDrawFrame(gl)
            }
            callbackIfNeeded()
        }
    }

    /**
     * 增加滤镜
     *
     * @param filter 滤镜
     */
    fun addFilter(filter: BaseFilter) {
        mGroupFilter!!.addFilter(filter)
    }

    fun takePhoto() {
        isShoot = true
    }

    fun setFrameCallback(width: Int, height: Int, frameCallback: FrameCallback?) {
        frameCallbackWidth = width
        frameCallbackHeight = height
        if (frameCallbackWidth > 0 && frameCallbackHeight > 0) {
            if (outPutBuffer != null) {
                outPutBuffer = arrayOfNulls(3)
            }
            calculateCallbackOM()
            mFrameCallback = frameCallback
        } else {
            mFrameCallback = null
        }
    }

    private fun calculateCallbackOM() {
        if (frameCallbackHeight > 0 && frameCallbackWidth > 0 && mDataSize!!.x > 0 && mDataSize!!.y > 0) { //计算输出的变换矩阵
            MatrixUtils.getMatrix(callbackOM, MatrixUtils.TYPE_CENTERCROP, mDataSize!!.x, mDataSize!!.y, frameCallbackWidth, frameCallbackHeight)
            MatrixUtils.flip(callbackOM, false, true)
        }
    }

    private fun sdkParamSet() {
        if (!isParamSet.get() && mDataSize!!.x > 0 && mDataSize!!.y > 0) {
            isParamSet.set(true)
        }
    }

    //需要回调，则缩放图片到指定大小，读取数据并回调
    private fun callbackIfNeeded() {
        if (mFrameCallback != null && isShoot) {
            indexOutput = if (indexOutput++ >= 2) 0 else indexOutput
            if (outPutBuffer!![indexOutput] == null) {
                outPutBuffer!![indexOutput] = ByteBuffer.allocate(frameCallbackWidth * frameCallbackHeight * 4)
            }
            GLES20.glViewport(0, 0, frameCallbackWidth, frameCallbackHeight)
            EasyGlUtils.bindFrameTexture(mExportFrame[0], mExportTexture[0])
            mShowFilter?.matrix = (callbackOM)
            mShowFilter!!.draw()
            frameCallback()
            isShoot = false
            EasyGlUtils.unBindFrameBuffer()
            mShowFilter?.matrix = (SM)
        }
    }

    //读取数据并回调
    private fun frameCallback() {
        GLES20.glReadPixels(0, 0, frameCallbackWidth, frameCallbackHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, outPutBuffer!![indexOutput])
        mFrameCallback!!.onFrame(outPutBuffer!![indexOutput]!!.array(), 0)
    }

    fun destroy() {
        if (mRenderer != null) {
            mRenderer!!.onDestroy()
        }
        mGLView!!.surfaceDestroyed(holder)
        mGLView!!.detachedFromWindow()
    }

    fun requestRender() {
        mGLView!!.requestRender()
    }

    fun onPause() {
        mGLView!!.onPause()
    }

    fun onResume() {
        mGLView!!.onResume()
    }

    /**
     * 自定义GLSurfaceView，暴露出onAttachedToWindow
     * 方法及onDetachedFromWindow方法，取消holder的默认监听
     * onAttachedToWindow及onDetachedFromWindow必须保证view
     * 存在Parent
     */
    private inner class GLView(context: Context?) : GLSurfaceView(context) {

        fun attachedToWindow() {
            super.onAttachedToWindow()
        }

        fun detachedFromWindow() {
            super.onDetachedFromWindow()
        }

        init {
            holder.addCallback(null)
            setEGLWindowSurfaceFactory(object : EGLWindowSurfaceFactory {
                override fun createWindowSurface(egl: EGL10, display: EGLDisplay, config: EGLConfig, window: Any): EGLSurface {
                    return egl.eglCreateWindowSurface(display, config, surface, null)
                }

                override fun destroySurface(egl: EGL10, display: EGLDisplay, surface: EGLSurface) {
                    egl.eglDestroySurface(display, surface)
                }
            })
            setEGLContextClientVersion(2)
            setRenderer(this@TextureController)
            renderMode = RENDERMODE_WHEN_DIRTY
            preserveEGLContextOnPause = true
        }
    }

    init {
        mGLView = GLView(mContext)
        //避免GLView的attachToWindow和detachFromWindow崩溃
        val v: ViewGroup = object : ViewGroup(mContext) {
            override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
        }
        v.addView(mGLView)
        v.visibility = View.GONE
        mEffectFilter = TextureFilter()
        mShowFilter = NoFilter()
        mGroupFilter = GroupFilter()
        //设置默认的DateSize
        mDataSize = Point(1080, 1920)
        mWindowSize = Point(1080, 1920)
    }

}