package com.tainzhi.sample.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.QuickViewHolder
import com.tainzhi.sample.media.camera.CameraActivity
import com.tainzhi.sample.media.native_codec.NativeCodecActivity
import com.tainzhi.sample.media.opengl2.BallActivity
import com.tainzhi.sample.media.opengl2.ConeActivity
import com.tainzhi.sample.media.opengl2.CylinderActivity
import com.tainzhi.sample.media.opengl2.ImageGLRendererActivity
import com.tainzhi.sample.media.opengl2.OvalActivity
import com.tainzhi.sample.media.opengl2.PaintPointActivity
import com.tainzhi.sample.media.opengl2.RotateTriangleActivity
import com.tainzhi.sample.media.opengl2.SquareActivity
import com.tainzhi.sample.media.opengl2.TriangleActivity
import com.tainzhi.sample.media.player.PlayActivity

/*
三种绘图方法
- 从asset目录导入图片流到bitmap绘制到ImageView，SurfaceView， 自定义View。
- canvas.drawBitmap使用Rect和scale matrix铺满
- `SurfaceHolder.Callback`的`onSurfaceCreated`之后会调用`onSurfaceChanged`
- [参考:Android 音视频开发(一) : 通过三种方式绘制图片](https://www.cnblogs.com/renhui/p/7456956.html)
 */
class MainActivity : AppCompatActivity() {
    
    private val datas = arrayListOf(
            ActivityItem("三种方式绘制图片",
                         "ImageView, SurfaceView, 自定义图片",
                         DrawImageActivity::class.java),
            ActivityItem("加载超级大图", "加载超级大图, 加载100M的图片不崩溃, 缩放等", LargeImageActivity::class.java),
            ActivityItem("录制音频, 播放音频", "AudioRecord采集音频PCM, 编码成WAV保存, 并用AudioTrack播放", AudioRecordPlayActivity::class.java),
            ActivityItem("硬解码视频播放器", "硬解码MediaCodec解码音频,视频并同步音视频", PlayActivity::class.java),
            ActivityItem("Camera2拍照录制音频视频", "Camera2拍照, 系统MediaRecorder录制音视频", CameraActivity::class.java),
            ActivityItem("OpenGLES draw triangle", "绘制基本三角形", TriangleActivity::class.java),
            ActivityItem("OpenGLES draw Square", "绘制立方体， 并缩放，位移，旋转等", SquareActivity::class.java),
            ActivityItem("OpenGLES draw oval", "绘制基本圆形", OvalActivity::class.java),
            ActivityItem("OpenGLES 绘制圆锥", "绘制基本圆锥体", ConeActivity::class.java),
            ActivityItem("OpenGLES 绘制圆柱体", "绘制基本圆柱体", CylinderActivity::class.java),
            ActivityItem("OpenGLES 绘制球体", "绘制发光小球", BallActivity::class.java),
            ActivityItem("OpenGLES 绘制画笔点", "响应触摸事件，并在触摸出绘制点", PaintPointActivity::class.java),
            ActivityItem("OpenGLES 旋转三角形", "响应触摸事件，旋转三角形", RotateTriangleActivity::class.java),
            ActivityItem("OpenGLES 渲染图片", "渲染效果: 黑白, 冷色, 暖色, 模糊, 放大", ImageGLRendererActivity::class.java),
            ActivityItem("OpenGLES && Native MediaCodec", "cpp层MediaCodec和GLSurfaceView", NativeCodecActivity::class.java))
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        findViewById<RecyclerView>(R.id.mainRecyclerView).run {
            layoutManager = LinearLayoutManager(context)
            adapter = MainAdapter().apply {
                setOnItemClickListener { _, _, position ->
                    startActivity(Intent(this@MainActivity, datas[position].clazz))
                }
                submitList(datas)
            }
            
        }
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
        Log.d(TAG, "onDestory()")
    }
    
    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart()")
    }
    
    companion object {
        private const val TAG = "MainActivity"
    }
}

data class ActivityItem(val title: String, val description: String, val clazz: Class<*>)

class MainAdapter() : BaseQuickAdapter<ActivityItem, QuickViewHolder>() {
    override fun onBindViewHolder(holder: QuickViewHolder, position: Int, item: ActivityItem?) {
        holder.setText(R.id.itemTitleTv, item?.title)
        holder.setText(R.id.itemDescriptionTv, item?.description)
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(R.layout.item_main, parent)
    }
}
