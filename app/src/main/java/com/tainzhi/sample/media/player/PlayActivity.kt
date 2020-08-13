package com.tainzhi.sample.media.player

import android.app.Activity
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import androidx.annotation.RequiresApi
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.util.toast
import kotlinx.android.synthetic.main.activity_play.*

class PlayActivity : Activity(), TextureView.SurfaceTextureListener, PlayerFeedback,
        View.OnClickListener,
        VideoPlayer.OnSizeChangedListener {
    
    companion object {
        private const val TAG: String = "PlayActivity"
    }
    
    private var textureView: TextureView? = null
    
    // 帧率 30
    private var current_fps = 30
    
    private lateinit var surface: Surface
    private lateinit var videoPlayer: VideoPlayer
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        
        textureView = tv_textureView
        textureView?.surfaceTextureListener = this
        
        ib_play.setOnClickListener(this)
        ib_play_slow.setOnClickListener(this)
        ib_play_slow.setOnClickListener(this)
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
                toast("当前帧率${current_fps}")
            }
            R.id.ib_play_fast -> {
                Log.d(TAG, "play fast")
                if (current_fps < 120) current_fps += 30
                videoPlayer.setSpeed(current_fps)
                toast("当前帧率${current_fps}")
            }
            else -> Unit
        }
    }
    
    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (::videoPlayer.isInitialized) videoPlayer.stop()
    }
    
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }
    
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        videoPlayer.stop()
        return true
    }
    
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        Log.d(TAG, "SurfaceTexture ready width:$width, height:$height")
        play()
    }
    
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
    }
    
    override fun playbackStopped() {
    }
    
    override fun onSizeChanged(width: Int, height: Int) {
        adjustAspectRatio(textureView!!, width, height)
    }
    
    @RequiresApi(Build.VERSION_CODES.N)
    private fun play() {
        if (this::videoPlayer.isInitialized) {
            if (videoPlayer.isPlaying) {
                videoPlayer.stop()
            } else {
                videoPlayer.start()
            }
            return
        }
        val surfaceTexture = textureView?.surfaceTexture
        surface = Surface(surfaceTexture)
        val video = assets.openFd("testfile.mp4")
        videoPlayer = VideoPlayer(video, surface)
        videoPlayer.setLoop(true)
        videoPlayer.start()
    }
    
    private fun adjustAspectRatio(textureView: TextureView, videoWidth: Int, videoHeight: Int) {
        val viewWidth = textureView.width
        val viewHeight = textureView.height
        val aspectRatio = videoHeight.toDouble() / videoWidth
        val newWidth: Int
        val newHeight: Int
        // limited by narrow width
        if (viewHeight > (viewWidth * aspectRatio).toInt()) {
            newWidth = viewWidth
            newHeight = (viewWidth * aspectRatio).toInt()
            // limited by narrow height
        } else {
            newWidth = (viewHeight / aspectRatio).toInt()
            newHeight = viewHeight
        }
        val xoff = (viewWidth - newWidth) / 2
        val yoff = (viewHeight - newHeight) / 2
        val txform = Matrix()
        textureView.getTransform(txform)
        txform.setScale(newWidth.toFloat() / viewWidth, newHeight.toFloat() / viewHeight)
        txform.postTranslate(xoff.toFloat(), yoff.toFloat())
        textureView.setTransform(txform)
        
    }
}
