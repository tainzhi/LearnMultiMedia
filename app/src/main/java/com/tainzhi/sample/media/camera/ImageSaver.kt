package com.tainzhi.sample.media.camera

import android.content.ContentValues
import android.media.Image
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
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

class ImageSaver(
        private val image: Image,
        private val file: File,
        private val handler: Handler?
) : Runnable {
    override fun run() {
        val relativeLocation = Environment.DIRECTORY_PICTURES
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (build)
        }
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file).apply {
                write(bytes)
            }
            
            val message = Message().apply {
                what = CAMERA_UPDATE_PREVIEW_PICTURE
                arg1 = uri.toString()
            }

            handler?.sendEmptyMessage(message)

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