package com.tainzhi.sample.media.opengl2.camera.preview

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.R

/**
 * 使用OpenGL预览摄像头界面
 */
class PreviewCameraActivity : AppCompatActivity() {
    private var mCameraView: CameraView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_preview)
        mCameraView = findViewById(R.id.camera_view)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("切换摄像头").setTitle("切换摄像头").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mCameraView != null) {
            mCameraView!!.switchCamera()
            showToast("切换摄像头")
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (mCameraView != null) {
            mCameraView!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mCameraView != null) {
            mCameraView!!.onPause()
        }
    }

    private fun showToast(msg: String) {
        runOnUiThread { Toast.makeText(this@PreviewCameraActivity, msg, Toast.LENGTH_LONG).show() }
    }
}