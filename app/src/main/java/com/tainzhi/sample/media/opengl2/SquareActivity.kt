package com.tainzhi.sample.media.opengl2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.opengl2.glsv.SquareGLSurfaceView

class SquareActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(SquareGLSurfaceView(this))
    }
}
