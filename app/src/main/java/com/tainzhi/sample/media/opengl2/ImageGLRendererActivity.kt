package com.tainzhi.sample.media.opengl2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.opengl2.glsv.ImageGLSurfaceView
import com.tainzhi.sample.media.opengl2.image.ImageTransformRenderer
import kotlinx.android.synthetic.main.activity_image_glrender.*

class ImageGLRendererActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_glrender)
        // setContentView(ImageGLSurfaceView(this))
        imageGLSurfaceView.setOnClickListener {
        
            (it as ImageGLSurfaceView).render =
                    ImageTransformRenderer(this, ImageTransformRenderer.Filter.COOL);  //
        }
    }
}
