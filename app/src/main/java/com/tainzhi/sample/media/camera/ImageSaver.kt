package com.tainzhi.sample.media.camera

import android.media.Image
import android.os.Handler
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/27 下午7:52
 * @description:
 **/

internal class ImageSaver(
        private val image: Image,
        private val file: File,
        private val handler: Handler?
) : Runnable {
    override fun run() {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }

            handler?.sendEmptyMessage(CAMERA_UPDATE_PREVIEW_PICTURE)

        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        } finally {
            image.close()
            output?.let {
                try {
                    it.close()
                } catch (e: IOException) {
                    Log.e(TAG, e.toString())
                }
            }
        }
    }

    companion object {
        private val TAG = "ImageSaver"
    }
}