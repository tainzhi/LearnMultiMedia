package com.tainzhi.sample.media.opengl2

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.R
import kotlinx.android.synthetic.main.activity_image_glrender.*
import java.util.concurrent.CopyOnWriteArrayList

class ImageGLRendererActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    
        setContentView(R.layout.activity_image_glrender)
    
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
        renderModePickerTextHint.data = arrayListOf<CharSequence>("黑白", "冷色调", "暖色调", "模糊", "黑白", "冷色调", "暖色调", "模糊")
    
    
        val bitmaps = CopyOnWriteArrayList<Bitmap>()
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        bitmaps.add(BitmapFactory.decodeResource(resources, R.drawable.huge))
        renderModePickerImageHint.data = bitmaps
    }
}
