package com.tainzhi.sample.media.opengl2.camera.filter

import android.hardware.Camera

class CameraFilter : OesFilter() {
    override fun initBuffer() {
        super.initBuffer()
    }//后置摄像头

    //前置摄像头
    override var flag: Int
        get() = flag
        set(flag) {
            if (flag == Camera.CameraInfo.CAMERA_FACING_FRONT) { //前置摄像头
                cameraFront()
            } else if (flag == Camera.CameraInfo.CAMERA_FACING_BACK) { //后置摄像头
                cameraBack()
            }
        }

    private fun cameraFront() {
        val coord = floatArrayOf(
                1.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f)
        mTexBuffer!!.clear()
        mTexBuffer!!.put(coord)
        mTexBuffer!!.position(0)
    }

    private fun cameraBack() {
        val coord = floatArrayOf(
                1.0f, 0.0f,
                0.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f)
        mTexBuffer!!.clear()
        mTexBuffer!!.put(coord)
        mTexBuffer!!.position(0)
    }
}