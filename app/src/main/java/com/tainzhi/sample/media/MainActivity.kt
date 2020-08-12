package com.tainzhi.sample.media

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.tainzhi.sample.media.camera.CameraActivity
import com.tainzhi.sample.media.native_codec.NativeCodecActivity
import com.tainzhi.sample.media.opengl2.*
import com.tainzhi.sample.media.opengl2.camera.CameraPreviewActivity
import com.tainzhi.sample.media.opengl2.camera.TakePictureActivity
import com.tainzhi.sample.media.player.PlayActivity
import java.util.*

class MainActivity : AppCompatActivity() {
    private var listView: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        listView = (findViewById<ListView>(R.id.listview)).apply {
            adapter = SimpleAdapter(this@MainActivity, createActivityList(),
                                    android.R.layout.two_line_list_item, arrayOf(TITLE, DESCRIPTION), intArrayOf(android.R.id.text1, android.R.id.text2))
            setOnItemClickListener { _, _, position, _ ->
                startActivity(Intent(context, testActivityBeans[position].clazz))
            }
        }
    }
    
    /**
     * Creates the list of activities from the string arrays.
     */
    private fun createActivityList(): List<Map<String, String>> {
        val testList: MutableList<Map<String, String>> = ArrayList()
        testActivityBeans.forEach {
            testList.add(mapOf(
                    TITLE to it.title,
                    DESCRIPTION to it.description
            ))
        }
        return testList
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume()")
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart()")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop()")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
    }
    
    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart()")
    }
    
    companion object {
        private const val TITLE = "title"
        private const val DESCRIPTION = "description"
        private const val TAG = "MainActivity"
        
        private val testActivityBeans = arrayOf(
                MainItemBean("通过三种方式绘制图片", "ImageView, SurfaceView, 自定义图片", DrawImageActivity::class.java),
                MainItemBean("加载超级大图", "加载超级大图, 加载100M的图片不崩溃, 缩放等", LargeImageActivity::class.java),
                MainItemBean("录制音频, 播放音频", "AudioRecord采集音频PCM, AudioTrack播放", AudioRecordPlayActivity::class.java),
                MainItemBean("kotlin用Camera2拍照", "MediaRecorder录制音视频", CameraActivity::class.java),
                
                MainItemBean("视频播放器", "MediaCodec实现的播放器", PlayActivity::class.java),
                MainItemBean("录制camera, 转码264, 混合音频", "camera预览数据,转码成h264,再混合音频", VideoRecordActivity::class.java),
                MainItemBean("OpenGLES draw triangle", "绘制基本三角形", TriangleActivity::class.java),
                MainItemBean("OpenGLES 绘制立方体", "绘制立方体， 并缩放，位移，旋转等", SquareActivity::class.java),
                
                MainItemBean("OpenGLES draw oval", "绘制基本圆形", OvalActivity::class.java),
                MainItemBean("OpenGLES 绘制圆锥", "绘制基本圆锥体", ConeActivity::class.java),
                MainItemBean("OpenGLES 绘制圆柱体", "绘制基本圆柱体", CylinderActivity::class.java),
                MainItemBean("OpenGLES 绘制球体", "绘制发光小球", BallActivity::class.java),
                
                MainItemBean("OpenGLES 绘制画笔点", "响应触摸事件，并在触摸出绘制点", PaintPointActivity::class.java),
                MainItemBean("OpenGLES 旋转三角形", "响应触摸事件，旋转三角形", RotateTriangleActivity::class.java),
                MainItemBean("OpenGLES 渲染图片", "渲染效果: 黑白, 冷色, 暖色, 模糊, 放大", ImageGLRendererActivity::class.java),
                MainItemBean("OpenGLES 预览相机", "预览相机", CameraPreviewActivity::class.java),
                
                MainItemBean("OpenGLES 相机拍照", "相机拍照", TakePictureActivity::class.java),
                MainItemBean("OpenGLES && Native MediaCodec", "cpp层MediaCodec和GLSurfaceView", NativeCodecActivity::class.java)
        )
    }
}

data class MainItemBean(val title: String, val description: String, val clazz: Class<*>)
