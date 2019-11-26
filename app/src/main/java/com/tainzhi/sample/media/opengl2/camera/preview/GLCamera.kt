package com.tainzhi.sample.media.opengl2.camera.preview

import android.graphics.Point
import android.graphics.SurfaceTexture
import android.hardware.Camera
import com.renhui.opengles20study.camera.preview.ICamera
import java.io.IOException
import java.util.*

/**
 * 使用后OpenGL预览的摄像头控制逻辑
 */
class GLCamera : ICamera {
    private val mConfig: ICamera.Config
    private var mCamera: Camera? = null
    private val sizeComparator: CameraSizeComparator
    override var previewSize: Point? = null
    private var picSize: Camera.Size? = null
    private var preSize: Camera.Size? = null
    /**
     * 打开摄像头
     *
     * @param cameraId 打开的摄像头的ID
     * @return 是否成功打开摄像头
     */
    override fun open(cameraId: Int): Boolean {
        mCamera = Camera.open(cameraId)
        if (mCamera != null) {
            val param = mCamera!!.parameters
            picSize = getPropPictureSize(param.supportedPictureSizes, mConfig.rate, mConfig.minPictureWidth)
            preSize = getPropPreviewSize(param.supportedPreviewSizes, mConfig.rate, mConfig.minPreviewWidth)
            param.setPictureSize(picSize!!.width, picSize!!.height)
            param.setPreviewSize(preSize!!.width, preSize!!.height)
            mCamera!!.parameters = param
            val pre = param.previewSize
            previewSize = Point(pre.height, pre.width)
            return true
        }
        return false
    }

    override fun preview(): Boolean {
        if (mCamera != null) {
            mCamera!!.startPreview()
        }
        return true
    }

    override fun close(): Boolean {
        if (mCamera != null) {
            try {
                mCamera!!.stopPreview()
                mCamera!!.release()
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }
        }
        return true
    }

    override fun setPreviewTexture(texture: SurfaceTexture?) {
        if (mCamera != null) {
            try {
                mCamera!!.setPreviewTexture(texture)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getPropPreviewSize(list: List<Camera.Size>, th: Float, minWidth: Int): Camera.Size {
        Collections.sort(list, sizeComparator)
        var i = 0
        for (s in list) {
            if (s.height >= minWidth && equalRate(s, th)) {
                break
            }
            i++
        }
        if (i == list.size) {
            i = 0
        }
        return list[i]
    }

    private fun getPropPictureSize(list: List<Camera.Size>, th: Float, minWidth: Int): Camera.Size {
        Collections.sort(list, sizeComparator)
        var i = 0
        for (s in list) {
            if (s.height >= minWidth && equalRate(s, th)) {
                break
            }
            i++
        }
        if (i == list.size) {
            i = 0
        }
        return list[i]
    }

    private fun equalRate(s: Camera.Size, rate: Float): Boolean {
        val r = s.width.toFloat() / s.height.toFloat()
        return if (Math.abs(r - rate) <= 0.03) {
            true
        } else {
            false
        }
    }

    private inner class CameraSizeComparator : Comparator<Camera.Size> {
        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int { // TODO Auto-generated method stub
            return if (lhs.height == rhs.height) {
                0
            } else if (lhs.height > rhs.height) {
                1
            } else {
                -1
            }
        }
    }

    init {
        mConfig = ICamera.Config()
        mConfig.minPreviewWidth = 720
        mConfig.minPictureWidth = 720
        mConfig.rate = 1.778f
        sizeComparator = CameraSizeComparator()
    }
}