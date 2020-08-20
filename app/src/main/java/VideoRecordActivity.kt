package com.tainzhi.sample.media

import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.tainzhi.sample.media.camera.Camera2BasicFragment

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/8/20 下午1:50
 * @description:
 **/

class VideoRecordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN)
        
        setContentView(R.layout.activity_camera)
        
        savedInstanceState ?: supportFragmentManager.beginTransaction()
                .replace(R.id.container, Camera2BasicFragment.newInstance())
                .commit()
    }
}