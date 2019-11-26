package com.tainzhi.sample.media.opengl2.camera.filter

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import com.tainzhi.sample.media.opengl2.camera.takepic.utils.MatrixUtils

/**
 * 水印 Filter
 */
class WaterMarkFilter : NoFilter() {
    private var mBitmap: Bitmap? = null
    private val mFilter: NoFilter
    private var width = 0
    private var height = 0
    private var x = 0
    private var y = 0
    private var w = 0
    private var h = 0
    override fun onCreate() {
        super.onCreate()
        mFilter.create()
        createTexture()
    }

    override fun onClear() {
        super.onClear()
    }

    override fun draw() {
        super.draw()
        GLES20.glViewport(x, y, if (w == 0) mBitmap!!.width else w, if (h == 0) mBitmap!!.height else h)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_DST_ALPHA)
        mFilter.draw()
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSizeChanged(width: Int, height: Int) {
        this.width = width
        this.height = height
        mFilter.setSize(width, height)
    }

    fun setWaterMark(bitmap: Bitmap?) {
        if (mBitmap != null) {
            mBitmap!!.recycle()
        }
        mBitmap = bitmap
    }

    private val textures = IntArray(1)
    private fun createTexture() {
        if (mBitmap != null) { //生成纹理
            GLES20.glGenTextures(1, textures, 0)
            //生成纹理
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
            //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST.toFloat())
            //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR.toFloat())
            //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE.toFloat())
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0)
            MatrixUtils.flip(mFilter.matrix, false, true)
            mFilter.textureId = textures[0]
        }
    }

    fun setPosition(x: Int, y: Int, width: Int, height: Int) {
        this.x = x
        this.y = y
        w = width
        h = height
    }

    init {
        mFilter = object : NoFilter() {
            override fun onClear() {}
        }
    }
}