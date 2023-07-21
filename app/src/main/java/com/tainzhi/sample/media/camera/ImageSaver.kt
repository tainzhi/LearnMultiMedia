package com.tainzhi.sample.media.camera

import android.content.ContentValues
import android.content.Context
import android.media.Image
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ArrayBlockingQueue

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/27 下午7:52
 * @description:
 **/

class ImageSaver(
    private val context: Context,
    private val imageQueue: ArrayBlockingQueue<Image>,
    private val handler: Handler
) : Runnable {
    override fun run() {
        val relativeLocation = Environment.DIRECTORY_PICTURES
        val image = imageQueue.take()
        val fileName = "IMG_${SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.US).format(image.timestamp)}.jpg"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.IS_PENDING, 0)
            }
        }
        val resolver = context.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        val buffer = image.planes[0].buffer
        val bytes = ByteArray(image.planes[0].buffer.remaining())
        buffer.get(bytes)

        try {
            imageUri?.let { uri ->
                val stream = resolver.openOutputStream(uri)
                if (stream != null) {
                    stream.write(bytes)

                    val message = Message().apply {
                        what = CAMERA_UPDATE_PREVIEW_PICTURE
                        obj = uri
                    }
                    handler.removeCallbacksAndMessages(null)
                    handler.sendMessage(message)
                } else {
                    throw IOException("Failed to create new MediaStore record")
                }
            }
        } catch (e: IOException) {
            imageUri?.let { resolver.delete(it, null, null) }
            throw IOException(e)
        } finally {
            // 必须关掉, 否则不能连续拍照
            while (imageQueue.size > 0) {
                imageQueue.take().close()
            }
        }
    }

    companion object {
        private val TAG = "ImageSaver"
    }
}
