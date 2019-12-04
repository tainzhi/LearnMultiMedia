package com.tainzhi.sample.media.player

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.R

class PlayActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, PlayerFeedback,
        View.OnClickListener {

    private var textureView: TextureView? = null

    private var surfaceTextureReady = false
    // 帧率 30
    private var current_fps = 30

    private lateinit var surface: Surface
    private lateinit var videoPlayer: VideoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        textureView = findViewById<View>(R.id.textureView) as TextureView
        textureView?.surfaceTextureListener = this

        findViewById<View>(R.id.ib_play_slow).setOnClickListener(this)
        findViewById<View>(R.id.ib_play).setOnClickListener(this)
        findViewById<View>(R.id.ib_play_fast).setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ib_play -> {
                play()
            }
            R.id.ib_play_slow -> {
                Log.d(TAG, "play slow")
                if (current_fps > 30) current_fps -= 30
                videoPlayer.setSpeed(current_fps)
                showToast(current_fps)
            }
            R.id.ib_play_fast -> {
                Log.d(TAG, "play fast")
                if (current_fps < 120) current_fps += 30
                videoPlayer.setSpeed(current_fps)
                showToast(current_fps)
            }
            else -> Unit
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (::videoPlayer.isInitialized) videoPlayer.stop()
    }

    private fun play() {
        if (surfaceTextureReady) {
            val surfaceTexture = textureView?.surfaceTexture
            surface = Surface(surfaceTexture)
            val videoPath = getExternalFilesDir(null)?.absolutePath + "/record.mp4"
            videoPlayer = VideoPlayer(videoPath, surface)
            videoPlayer.setLoop(true)
            videoPlayer.start()
        }
    }

    private fun showToast(fps: Int) {
        Toast.makeText(this@PlayActivity, "当前帧率为$fps", Toast.LENGTH_SHORT).show()
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
