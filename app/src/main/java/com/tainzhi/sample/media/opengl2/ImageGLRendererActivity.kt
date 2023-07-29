package com.tainzhi.sample.media.opengl2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.databinding.ActivityImageGlrenderBinding
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

class ImageGLRendererActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageGlrenderBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageGlrenderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // binding.imageGLSurfaceView.bitmap = BitmapFactory.decodeResource(resources, R.drawable.huge)
        try {
            val inputStream = assets.open("img.png");
            binding.imageGLSurfaceView.bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (e: IOException) {
            Log.d(TAG, Log.getStackTraceString(e))
        }

        // var imageUri: Uri? = null
        // if (intent.action == Intent.ACTION_VIEW) {
        //     imageUri = intent.data
        // }
        // imageGLSurfaceView.render = ImageRenderer(this)
        // if (imageUri != null) {
        //     try {
        //         imageGLSurfaceView.bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        //             MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        //         } else { ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, imageUri))}
        //     } catch (e: IOException) {
        //         e.printStackTrace()
        //     }
        // }
        binding.renderModePickerTextHint.data = arrayListOf<CharSequence>("黑白", "冷色调", "暖色调", "模糊", "黑白", "冷色调", "暖色调", "模糊")

        val bitmaps = CopyOnWriteArrayList<Bitmap>()
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        binding.renderModePickerImageHint.data = bitmaps
    }

    companion object {
        private val TAG = ImageGLRendererActivity::class.java.simpleName
    }
}
