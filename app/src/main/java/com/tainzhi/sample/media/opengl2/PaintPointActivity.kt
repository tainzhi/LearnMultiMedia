import com.tainzhi.sample.media.R

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tainzhi.sample.media.opengl2.glsv.PaintPointGLSurfaceView

class PaintPointActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(PaintPointGLSurfaceView(this))
    }
}
