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
import java.io.File
import java.io.IOException

class PlayActivity : AppCompatActivity(), TextureView.SurfaceTextureListener, PlayerFeedback,
        View.OnClickListener {

    private var textureView: TextureView? = null

    private lateinit var playTask: PlayTask
    private lateinit var speedControl: SpeedControlCallback
    private var surfaceTextureReady = false
    private val filePath: String? = null
    // 帧率 30
    private var current_fps = 30

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
                speedControl.setFixedPlaybackRate(current_fps)
                showToast(current_fps)
            }
            R.id.ib_play_fast -> {
                Log.d(TAG, "play fast")
                if (current_fps < 120) current_fps += 30
                speedControl.setFixedPlaybackRate(current_fps)
                showToast(current_fps)
            }
            else -> Unit
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (::playTask.isInitialized) playTask.requestStop()
    }

    private fun play() {
        if (surfaceTextureReady) {
            val surfaceTexture = textureView?.surfaceTexture
            val surface = Surface(surfaceTexture)
            val player: MoviePlayer
            speedControl = SpeedControlCallback()
            val filePath = getExternalFilesDir(null)?.absolutePath + "/record.mp4"
            player = try {
                MoviePlayer(File(filePath), surface, speedControl)
            } catch (e: IOException) {
                Log.e(TAG, "Unable to play movie $e")
                surface.release()
                return
            }
            player.setLoopMode(true)
            playTask = PlayTask(player, this)
            playTask.execute()
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
