package com.tainzhi.sample.media.opengl2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.opengl2.glsv.ImageGLSurfaceView

class ImageGLRendererActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ImageGLSurfaceView(this))
    }
}
