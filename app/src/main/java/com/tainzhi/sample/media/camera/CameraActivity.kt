package com.tainzhi.sample.media.camera

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.com.tainzhi.sample.media.camera.PermissionsFragment

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/27 上午11:14
 * @description:
 **/

class CameraActivity : AppCompatActivity(R.layout.activity_camera) {

    private lateinit var fragmentContainerView: FragmentContainerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fragmentContainerView = findViewById(R.id.camera_container)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.camera_container, PermissionsFragment())
            }
        }
        setFullScreen()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = WindowCompat.getInsetsController(window, fragmentContainerView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
//            fragmentContainerView.postDelayed ({
//                fragmentContainerView.systemUiVisibility = FLAGS_FULLSCREEN}, 500L)
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )

        }
    }

    companion object {
        @TargetApi(Build.VERSION_CODES.Q)
        const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }
}