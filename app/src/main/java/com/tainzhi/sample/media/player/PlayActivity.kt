package com.tainzhi.sample.media.player

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.R
import java.io.File
import java.io.IOException

class PlayActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, PlayerFeedback,
        View.OnClickListener {

    private var textureView: TextureView? = null
    private var control: ImageButton? = null

    private var playTask: PlayTask? = null
    private var surfaceTextureReady = false
    private val filePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        textureView = findViewById<View>(R.id.textureView) as TextureView
        control = findViewById<View>(R.id.control) as ImageButton
        textureView?.surfaceTextureListener = this
        control?.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.control -> {
                play()
            }
            else -> Unit
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        playTask!!.requestStop()
    }

    fun play() {
        if (surfaceTextureReady) {
            val surfaceTexture = textureView?.surfaceTexture
            val surface = Surface(surfaceTexture)
            var player: MoviePlayer? = null
            val callback: SpeedControlCallback = SpeedControlCallback()
            val filePath = getExternalFilesDir(null)?.absolutePath + "/record.mp4"
            player = try {
                MoviePlayer(File(filePath), surface, callback)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to play movie $e")
                surface.release()
                return
            }
            playTask = PlayTask(player, this)
            playTask!!.execute()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        surfaceTextureReady = false
        return true
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Log.d(TAG, "SurfaceTexture ready width:$width, height:$height")
        surfaceTextureReady = true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }

    override fun playbackStopped() {
    }

    companion object {
        private const val TAG: String = "PlayActivity"
    }
}
