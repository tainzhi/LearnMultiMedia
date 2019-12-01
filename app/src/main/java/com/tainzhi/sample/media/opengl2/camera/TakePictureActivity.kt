package com.tainzhi.sample.media.opengl2.camera

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.opengl2.camera.filter.WaterMarkFilter
import com.tainzhi.sample.media.opengl2.camera.takepic.FrameCallback
import com.tainzhi.sample.media.opengl2.camera.takepic.Renderer
import com.tainzhi.sample.media.opengl2.camera.takepic.TextureController
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * OpenGL拍照
 */
class TakePictureActivity : Activity(), FrameCallback {
    private var mSurfaceView: SurfaceView? = null
    private var mController: TextureController? = null
    private var mRenderer: Renderer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mRenderer = CameraRenderer()
        setContentView(R.layout.activity_takepic)
        mSurfaceView = findViewById(R.id.mSurface)
        mController = TextureController(this@TakePictureActivity)
        // 添加水印
        val filter = WaterMarkFilter()
        filter.setWaterMark(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        filter.setPosition(600, 500, 192, 192)
        mController!!.addFilter(filter)
        mController!!.setFrameCallback(1080, 1920, this@TakePictureActivity)
        mSurfaceView?.getHolder()?.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mController!!.surfaceCreated(holder)
                mController!!.setRenderer(mRenderer)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                mController!!.surfaceChanged(width, height)
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                mController!!.surfaceDestroyed()
            }
        })
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.mShutter -> mController!!.takePhoto()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mController != null) {
            mController!!.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mController != null) {
            mController!!.onPause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mController != null) {
            mController!!.destroy()
        }
    }

    override fun onFrame(bytes: ByteArray?, time: Long) {
        Thread(Runnable {
            val bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888)
            val b = ByteBuffer.wrap(bytes)
            bitmap.copyPixelsFromBuffer(b)
            saveBitmap(bitmap)
            bitmap.recycle()
        }).start()
    }

    protected val sD: String
        protected get() = getExternalFilesDir(null).toString();

    //图片保存
    fun saveBitmap(b: Bitmap) {
        val path = "$sD/"
        val folder = File(path)
        if (!folder.exists() && !folder.mkdirs()) {
            runOnUiThread { Toast.makeText(this@TakePictureActivity, "无法保存照片", Toast.LENGTH_SHORT).show() }
            return
        }
        val dataTake = System.currentTimeMillis()
        val jpegName = "$path$dataTake.jpg"
        try {
            val fout = FileOutputStream(jpegName)
            val bos = BufferedOutputStream(fout)
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos)
            bos.flush()
            bos.close()
        } catch (e: IOException) { // TODO Auto-generated catch block
            e.printStackTrace()
        }
        runOnUiThread { Toast.makeText(this@TakePictureActivity, "保存成功->$jpegName", Toast.LENGTH_SHORT).show() }
    }

    private inner class CameraRenderer : Renderer {
        private var mCamera: Camera? = null
        override fun onDestroy() {
            if (mCamera != null) {
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
            if (mCamera != null) {
                mCamera!!.stopPreview()
                mCamera!!.release()
                mCamera = null
            }
            mCamera = Camera.open(cameraId)
            mController!!.setImageDirection(cameraId)
            val size = mCamera?.getParameters()?.previewSize // TODO 还可以拍的更高清
            // getSupportedPreviewSizes
            Log.e("111", size?.width.toString() + "////" + size?.height)
            mController!!.setDataSize(size?.height, size?.width)
            try {
                mCamera?.setPreviewTexture(mController?.texture)
                mController?.texture?.setOnFrameAvailableListener { mController!!.requestRender() }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mCamera?.startPreview()
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {}
        override fun onDrawFrame(gl: GL10) {}
    }

    companion object {
        private const val cameraId = 1 // 要打开的摄像头的ID
    }
}