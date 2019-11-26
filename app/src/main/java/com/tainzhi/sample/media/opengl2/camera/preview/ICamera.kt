package com.renhui.opengles20study.camera.preview

import android.graphics.Point
import android.graphics.SurfaceTexture

/**
 * 定义了一些摄像头事件的接口
 */
interface ICamera {
    /**
     * 打开摄像头
     *
     * @param cameraId 打开的摄像头的ID
     * @return 是否成功打开摄像头
     */
    fun open(cameraId: Int): Boolean

    /**
     * 预览摄像头内容
     */
    fun preview(): Boolean

    /**
     * 关闭摄像头
     */
    fun close(): Boolean

    /**
     * 设置展示摄像头的图像数据的容器
     * @param texture
     */
    fun setPreviewTexture(texture: SurfaceTexture?)

    /**
     * 获取预览尺寸
     */
    val previewSize: Point?

    class Config {
        var rate = 0f//宽高比 = 0f
        var minPreviewWidth = 0
        var minPictureWidth = 0
    }
}