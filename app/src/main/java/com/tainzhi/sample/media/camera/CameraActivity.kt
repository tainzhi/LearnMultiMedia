package com.tainzhi.sample.media.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.hardware.camera2.params.InputConfiguration
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.media.ImageWriter
import android.media.MediaActionSound
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import android.net.Uri
import android.opengl.GLES20
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.ExecutorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.camera.CameraInfoCache.Companion.chooseOptimalSize
import com.tainzhi.sample.media.camera.ui.ControlBar
import com.tainzhi.sample.media.camera.util.RotationChangeListener
import com.tainzhi.sample.media.camera.util.RotationChangeMonitor
import com.tainzhi.sample.media.camera.util.SettingsManager
import com.tainzhi.sample.media.databinding.ActivityCameraBinding
import com.tainzhi.sample.media.util.Kpi
import com.tainzhi.sample.media.util.toast
import com.tainzhi.sample.media.widget.CircleImageView
import java.io.IOException
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/27 上午11:14
 * @description:
 **/

class CameraActivity : AppCompatActivity() {

    private lateinit var rootView: View
    private lateinit var _binding: ActivityCameraBinding

    private val permissions_exclude_storage = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    )
    private val permission_storage = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private val unGrantedPermissionList: MutableList<String> = ArrayList()

    // to play click sound when take picture
    private val mediaActionSound = MediaActionSound()

    private lateinit var previewView: CameraPreviewView
    private lateinit var cameraPreviewRenderer: CameraPreviewRender

    // 预览拍照的图片，用于相册打开
    private lateinit var ivThumbnail: CircleImageView
    private lateinit var ivTakePicture: ImageView
    private lateinit var ivRecord: ImageView
    private lateinit var btnSwitchCamera: ImageButton

    private lateinit var capturedImageUri: Uri
    private lateinit var cameraInfo: CameraInfoCache

    // default open front-facing cameras/lens
    private var useCameraFront = false

    private lateinit var rotationChangeMonitor: RotationChangeMonitor
    private var thumbnailOrientation = 0

    private var isEnableZsl = SettingsManager.getInstance()!!
            .getBoolean(SettingsManager.KEY_PHOTO_ZSL, SettingsManager.PHOTO_ZSL_DEFAULT_VALUE)

    private var surfaceTextureListener = object : CameraPreviewView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: ${width}x${height}")
            GLES20.glViewport(0, 0, width, height)
            viewSize = Size(width, height)
//            previewAspectRatio = viewSize.height/viewSize.width.toFloat()
            val previewAspectRatio = SettingsManager.getInstance()!!.getPreviewAspectRatio()
            cameraPreviewRenderer.setViewSize(width, height)
            val ratioValue: Float = when (previewAspectRatio) {
                SettingsManager.PreviewAspectRatio.RATIO_1x1 -> 1.toFloat()
                SettingsManager.PreviewAspectRatio.RATIO_4x3 -> 4 / 3.toFloat()
                SettingsManager.PreviewAspectRatio.RATIO_16x9 -> 16 / 9.toFloat()
                SettingsManager.PreviewAspectRatio.RATIO_FULL -> height / width.toFloat()
                else -> height / width.toFloat()
            }
            setUpCameraPreview(ratioValue)
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            Log.d(TAG, "onSurfaceTextureDestroyed: ")
            return true
        }

        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            previewView.requestRender()
        }

        override fun onSurfaceTextureCreated(surface: SurfaceTexture, width: Int, height: Int) {
            Log.d(TAG, "onSurfaceTextureCreated: ${width}x${height}")
            previewSurface = Surface(surface)
            openCamera(width, height)
        }
    }

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)
    private var cameraExecutor = ExecutorCompat.create(cameraHandler)

    private var imageReaderThread = HandlerThread("ImageReaderThread").apply { start() }
    private val imageReaderHandler = Handler(imageReaderThread.looper) { msg ->
        when (msg.what) {
            CAMERA_UPDATE_PREVIEW_PICTURE -> {
                val pictureUri: Uri = msg.obj as Uri
                capturedImageUri = pictureUri
                Kpi.start(Kpi.TYPE.IMAGE_TO_THUMBNAIL)
                updateThumbnail(pictureUri)
            }
        }
        false
    }

    private lateinit var lastTotalCaptureResult: TotalCaptureResult
    private lateinit var zslImageWriter: ImageWriter

    // a [Semaphore] to prevent the app from exiting before closing the camera
    private val cameraOpenCloseLock = Semaphore(1)

    private lateinit var cameraId: String

    // for camera preview
    private var currentCaptureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null

    // default set to full screen size
    private lateinit var viewSize: Size

    // camera output surface size, maybe smaller than viewSize
    // e.g. set camera preview 1:1 for a device 1080:2040, then previewSize 1080:1080, viewSize 1080:2040
    private lateinit var previewSize: Size

    private var flashSupported = false

    // orientation of the camera sensor
    private var sensorOrientation: Int? = 0

    private var cameraState = STATE_PREVIEW

    // handles still image capture
    private lateinit var jpgImageReader: ImageReader
    private val jpgImageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
    private lateinit var yuvImageReader: ImageReader
    private var yuvLatestReceivedImage: Image? = null

    private var isRecordingVideo = false
    private var mediaRecorder: MediaRecorder? = null
    private var videoPath: String? = null
    private lateinit var videoSize: Size

    private lateinit var previewRequestBuilder: CaptureRequest.Builder
    private lateinit var previewRequest: CaptureRequest
    private lateinit var previewSurface: Surface

    private val cameraDeviceCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(p0: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraActivity.cameraDevice = p0
            Log.i(TAG, "camera onOpened: ")
            startCaptureSession()
        }

        override fun onDisconnected(p0: CameraDevice) {
            super.onClosed(p0)
            Log.i(TAG, "camera onClosed: ")
            cameraOpenCloseLock.release()
            p0.close()
            this@CameraActivity.cameraDevice = null
        }

        override fun onError(p0: CameraDevice, p1: Int) {
            onDisconnected(p0)
            Log.i(TAG, "onError: $p0, $p1")
            finish()
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (cameraState) {
                STATE_PREVIEW -> {
                }

                STATE_WAITING_LOCK -> capturePicture(result)
                STATE_WAITING_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED
                    ) {
                        cameraState = STATE_WAITING_NON_PRECAPTURE
                    }
                }

                STATE_WAITING_NON_PRECAPTURE -> {
                    // CONTROL_AE_STATE can be null on some devices
                    val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        cameraState = STATE_PICTURE_TAKEN
//                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                Log.e(TAG, "capturePicture: afState is null")
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                    || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
            ) {
                // CONTROL_AE_STATE can be null on some devices
                Log.e(TAG, "capturePicture: afState is ${afState}")
                val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
                if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                    cameraState = STATE_PICTURE_TAKEN
                    captureStillPicture()
                } else {
                    runPrecaptureSequence()
                }
            }
        }

        override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
        ) {
            lastTotalCaptureResult = result
            process(result)
        }

        override fun onCaptureProgressed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                partialResult: CaptureResult
        ) {
            process(partialResult)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        rootView = _binding.root
        setFullScreen()
        ControlBar(this, _binding) {
            Log.d(TAG, "onPreviewAspectyRationChange")
        }

        previewView = findViewById<CameraPreviewView>(R.id.previewView).apply {
            cameraPreviewRenderer = CameraPreviewRender()
            setRender(cameraPreviewRenderer)
        }
        ivThumbnail = findViewById<CircleImageView>(R.id.iv_thumbnail).apply {
            setOnClickListener {
                viewPicture()
            }
        }
        ivTakePicture = findViewById<ImageView>(R.id.picture).apply {
            setOnClickListener {
                // Most device front lenses/camera have a fixed focal length
                if (useCameraFront) captureStillPicture() else lockFocus()
            }
        }
        ivRecord = findViewById<ImageView>(R.id.iv_record).apply {
            setOnClickListener {
                if (isRecordingVideo) stopRecordingVideo() else startRecordingVideo()
            }
        }
        btnSwitchCamera = findViewById<ImageButton>(R.id.iv_switch_camera).apply {
            setOnClickListener {
                useCameraFront = !useCameraFront
                closeCamera()
                openCamera(previewView.width, previewView.height)
            }
        }

        _binding.cameraModePicker.apply {
            data = cameraModes.toList()
            setOnSelectedListener { _, position ->
                when (position) {
                    CaptureMode -> {
                        ivRecord.visibility = View.INVISIBLE
                        ivTakePicture.visibility = View.VISIBLE
                    }

                    RecordMode -> {
                        ivRecord.visibility = View.VISIBLE
                        ivTakePicture.visibility = View.INVISIBLE
                    }

                    BroadcastModel -> {
                        toast("待实现录制视频推送功能")
                    }
                }
            }
        }

        mediaActionSound.load(MediaActionSound.SHUTTER_CLICK)
        checkPermissions()
        rotationChangeMonitor = RotationChangeMonitor(this).apply {
            rotationChangeListener = object : RotationChangeListener {
                override fun onRotateChange(oldOrientation: Int, newOrientation: Int) {
                    Log.d(TAG, "orientation change from ${oldOrientation} -> ${newOrientation}")
                    handleRotation(newOrientation - oldOrientation)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume: ")
        previewView.onResume()
        rotationChangeMonitor.enable()
        if (false) {
            openCamera(previewView.width, previewView.height)
        } else {
            previewView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        Log.i(TAG, "onPause: ")
        rotationChangeMonitor.disable()
        previewView.onPause()
        closeCamera()
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy: ")
        imageReaderThread.quitSafely()
        cameraThread.quitSafely()
        mediaActionSound.release()
        super.onDestroy()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST) {
            for (i in grantResults.indices) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, permissions[i] + " block")
                } else {
                    Log.d(TAG, permissions[i] + " grand")
                }
            }
        } else {
            // TODO: 2019-11-22 运行时权限的申请
            Log.i(TAG, "onRequestPermissionsResult: ")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged")
    }

    private fun openCamera(width: Int, height: Int) {
        Log.i(TAG, "openCamera: ")
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        setUpCameraOutputs(manager, width, height)
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.e(TAG, "openCamera: not grand permission")
                return
            }
            manager.openCamera(cameraId, cameraDeviceCallback, cameraHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun closeCamera() {
        Log.i(TAG, "closeCamera: ")
        try {
            cameraOpenCloseLock.acquire()
            currentCaptureSession?.close()
            currentCaptureSession = null
            cameraDevice?.close()
            cameraDevice = null
            jpgImageReader.close()
            if (isEnableZsl) {
                yuvImageReader.close()
                zslImageWriter.close()
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
            Log.i(TAG, "closeCamera: released")
        }
    }

    private fun checkPermissions() {
        // Marshmallow开始运行时申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions_exclude_storage) {
                if (ContextCompat.checkSelfPermission(this, permission)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    unGrantedPermissionList.add(permission)
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(this, permission_storage)
                        != PackageManager.PERMISSION_GRANTED
                ) {
                    unGrantedPermissionList.add(permission_storage)
                }
            }
        }
        if (!unGrantedPermissionList.isEmpty()) {
            val tmpPermissions = unGrantedPermissionList.toTypedArray()
            Log.d(TAG, "checkPermissions: size=" + tmpPermissions.size)
            ActivityCompat.requestPermissions(this, tmpPermissions, MY_PERMISSIONS_REQUEST)
        }
    }


    private fun setFullScreen() {
        // todo:
        // reference https://developer.android.com/develop/ui/views/layout/edge-to-edge
        // to  solve immersive mode mode but transparent navigation bar
        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, rootView)
            controller?.isAppearanceLightNavigationBars = true
            controller.hide(WindowInsetsCompat.Type.statusBars())

            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
                // Apply the insets as padding to the view. Here we're setting all of the
                // dimensions, but apply as appropriate to your layout. You could also
                // update the views margin if more appropriate.
                view.updatePadding(insets.left, insets.top, insets.right, insets.bottom)

                // Return CONSUMED if we don't want the window insets to keep being passed
                // down to descendant views.
                WindowInsetsCompat.CONSUMED
            }
        } else {
            // it doesn't work when set transparent for statusbar/navigationbar in styles.xml
            // so hardcode here
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(cameraManager: CameraManager, width: Int, height: Int) {
        try {
            cameraInfo = CameraInfoCache(cameraManager, useCameraFront)
            isEnableZsl = cameraInfo.isSupportReproc() &&
                    SettingsManager.getInstance()!!
                            .getBoolean(SettingsManager.KEY_PHOTO_ZSL, SettingsManager.PHOTO_ZSL_DEFAULT_VALUE)
            cameraId = cameraInfo.cameraId
            videoSize = cameraInfo.videoSize
            jpgImageReader = ImageReader.newInstance(
                    cameraInfo.largestJpgSize.width, cameraInfo.largestJpgSize.height,
                    ImageFormat.JPEG, IMAGE_BUFFER_SIZE
            )
            jpgImageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireLatestImage()
                Log.d(TAG, "setUpCameraOutputs: jpgImageQueue add")
                jpgImageQueue.add(image)
                // val data = YUVTool.getBytesFromImageReader(it)
                // val myMediaRecorder =  MyMediaRecorder()
                // myMediaRecorder.addVideoData(data)
            }, imageReaderHandler)
            if (isEnableZsl) {
                yuvImageReader = ImageReader.newInstance(
                        cameraInfo.largestYuvSize.width, cameraInfo.largestYuvSize.height,
                        ImageFormat.YUV_420_888,
                        YUV_IMAGE_READER_SIZE
                )
                yuvImageReader.setOnImageAvailableListener({ reader ->
                    val image = reader.acquireLatestImage()
                    yuvLatestReceivedImage?.close()
                    yuvLatestReceivedImage = image
                }, cameraHandler)
            }
            flashSupported = cameraInfo.isflashSupported

        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: Exception) {
            Log.e(TAG, Log.getStackTraceString(e))
            ErrorDialog.newInstance(getString(R.string.camera_error))
                    .show(supportFragmentManager, "fragment_dialog")
        }
    }

    private fun setUpCameraPreview(ratioValue: Float) {
        // device display rotation
        // 0 [Surface.ROTATION_0]{android.view.Surface.ROTATION_0 = 0}  -> portrait, 把手机垂直放置且屏幕朝向我们的时候，即设备自然方向
        // 90  -> landscape, 手机向右横放(前置镜头在右边)且屏幕朝向我们的时候，
        // 180 -> portrait, 手机竖着倒放且屏幕朝我我们
        // 270 -> 手机向左横放且屏幕朝向我们
        val displayRotation = windowManager?.defaultDisplay?.rotation
        // reference: https://developer.android.com/training/camera2/camera-preview#device_rotation
        // 对于大多数设备，portrait且屏幕面向用户
        // 前置镜头 sensorOrientation = 270, 所以要相对于设备方向逆时针旋转 270
        // 后置镜头 sensorOrientation = 90, 所以要相对于设备方向逆时针旋转 90
        // 最终的 rotation = (sensorOrientationDegrees - deviceOrientationDegrees * sign + 360) % 360
        // sign = 1, 前置镜头，-1 后置镜头
        sensorOrientation = cameraInfo.sensorOrientation
        Log.d(TAG, "setUpCameraOutputs: displayRotation=$displayRotation, sensorOrientation=$sensorOrientation")
        // device portrait orientation ,then deviceHeight > deviceWidth
        // device landscape orientation, then deviceHeight < deviceWidth
        // whether device orientation, sensorWidth > deviceHeight is always true
        val swappedDimensions = areDimensionsSwapped(displayRotation)
        if (swappedDimensions) {
            Log.d(TAG, "setUpCameraOutputs: rotate switch width/height")
            viewSize = Size(viewSize.height, viewSize.width)
        }
        val (chosenSize, chosenSizeAspectRatio) = chooseOptimalSize(
                cameraInfo.getOutputPreviewSurfaceSize(),
                viewSize,
                ratioValue,
                true
        )
        val previewAspectRatio = SettingsManager.getInstance()?.getPreviewAspectRatio()
        Log.d(TAG, "viewSize: ${viewSize}, previewSize:${chosenSize}," +
                "previewAspectRatio=${previewAspectRatio}-${ratioValue}, chosen previewSizeAspectRatio:${chosenSizeAspectRatio}"
        )
        previewSize = chosenSize
        cameraPreviewRenderer.setDataSize(previewSize.width, previewSize.height)
    }


    // device portrait orientation ,then deviceHeight > deviceWidth
    // device landscape orientation, then deviceHeight < deviceWidth
    // whether device orientation, sensorWidth > deviceHeight is always true
    private fun areDimensionsSwapped(displayRotation: Int?): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = false
                }
            }

            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (sensorOrientation == 0 || sensorOrientation == 180) {
                    swappedDimensions = true
                }
            }

            else -> {
                Log.e(TAG, "Display rotation is invalid: $displayRotation")
            }
        }
        return swappedDimensions
    }

    private fun startCaptureSession() {
        Log.i(TAG, "startCaptureSession: ")
        try {
            val captureSessionStateCallback = object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(p0: CameraCaptureSession) {
                    toast("Failed")
                }

                override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                }

                override fun onReady(session: CameraCaptureSession) {
                    // When the session is ready, we start displaying the preview.
                    super.onReady(session)
                    Log.d(TAG, "capture session onReady()")
                    currentCaptureSession = session
                    updatePreview()
                    Log.d(TAG, "startCaptureSession onReady isReprocessable=${session.isReprocessable} ")
                    if (session.isReprocessable) {
                        zslImageWriter = ImageWriter.newInstance(session.inputSurface!!, ZSL_IMAGE_WRITER_SIZE)
                        zslImageWriter.setOnImageReleasedListener({ _ ->
                            {
                                Log.d(TAG, "ZslImageWriter onImageReleased()")
                            }
                        }, cameraHandler)
                        Log.d(TAG, "create ImageWriter")
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val previewConfiguration = OutputConfiguration(previewSurface)

                val outputConfigurations = mutableListOf<OutputConfiguration>(
                        previewConfiguration,
                        OutputConfiguration(jpgImageReader.surface)
                )
                if (isEnableZsl) {
                    outputConfigurations.add(
                            OutputConfiguration(yuvImageReader.surface)
                    )
                }
                val sessionConfiguration = SessionConfiguration(
                        SessionConfiguration.SESSION_REGULAR,
                        outputConfigurations,
                        cameraExecutor!!,
                        captureSessionStateCallback
                )
                if (isEnableZsl) {
                    sessionConfiguration.inputConfiguration =
                            InputConfiguration(yuvImageReader.width, yuvImageReader.height, ImageFormat.YUV_420_888)
                }
                cameraDevice?.createCaptureSession(sessionConfiguration)
            } else {
                if (isEnableZsl) {

                } else {
                    cameraDevice?.createCaptureSession(
                            arrayListOf(previewSurface, jpgImageReader?.surface),
                            captureSessionStateCallback, cameraHandler
                    )
                }

            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun updatePreview() {
        if (cameraDevice == null) return
        try {
            previewRequestBuilder =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            previewRequestBuilder.addTarget(previewSurface)
            if (isEnableZsl && this::yuvImageReader.isInitialized) {
                previewRequestBuilder.addTarget(yuvImageReader.surface)
            }

            previewRequestBuilder.apply {
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.NOISE_REDUCTION_MODE, cameraInfo.getCaptureNoiseMode())
                set(CaptureRequest.EDGE_MODE, cameraInfo.getEdgeMode())
                if (flashSupported) {
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                }
            }
//            if (AFtrigger) {
//                b1.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
//                mCurrentCaptureSession.capture(b1.build(), mCaptureCallback, mOpsHandler);
//                b1.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val controlZsl: Boolean? = previewRequestBuilder.get(CaptureRequest.CONTROL_ENABLE_ZSL)
                Log.d(TAG, "CaptureRequest: controlZsl=${controlZsl}")
            }
            previewRequest = previewRequestBuilder.build()
            currentCaptureSession?.setRepeatingRequest(
                    previewRequest,
                    captureCallback, cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    /**
     * Run the precapture sequence for capturing a still image. This method should be called when
     * we get a response in [.captureCallback] from [.lockFocus].
     */
    private fun runPrecaptureSequence() {
        try {
            previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                    CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
            cameraState = STATE_WAITING_PRECAPTURE
            currentCaptureSession?.capture(
                    previewRequestBuilder.build(), captureCallback,
                    cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    /**
     * Capture a still picture. This method should be called when we get a response in
     * [.captureCallback] from both [.lockFocus].
     */
    private fun captureStillPicture() {
        Log.d(TAG, "captureStillPicture: enableZsl=${isEnableZsl}")
        mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)
        Kpi.start(Kpi.TYPE.SHOT_TO_SHOT)
        try {
            if (isEnableZsl && yuvLatestReceivedImage == null) {
                Log.e(TAG, "captureStillPicture: no yuv image available")
                return
            }
            val rotation = windowManager.defaultDisplay.rotation

            val captureBuilder =
                    if (isEnableZsl && this::zslImageWriter.isInitialized) {
                        Log.d(TAG, "captureStillPicture: queueInput yuvLatestReceiveImage to HAL")
                        zslImageWriter.queueInputImage(yuvLatestReceivedImage)
                        cameraDevice!!.createReprocessCaptureRequest(lastTotalCaptureResult)
                    } else {
                        cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                    }
            captureBuilder.apply {
                addTarget(jpgImageReader.surface)
                if (isEnableZsl) {
                    set(CaptureRequest.NOISE_REDUCTION_MODE, cameraInfo.reprocessingNoiseMode);
                    set(CaptureRequest.EDGE_MODE, cameraInfo.reprocessingEdgeMode);
                }
                set(CaptureRequest.JPEG_QUALITY, 95);
                // https://developer.android.com/training/camera2/camera-preview#orientation_calculation
                // rotation = (sensorOrientationDegrees - deviceOrientationDegrees * sign + 360) % 360
                // sign 1 for front-facing cameras, -1 for back-facing cameras
                set(
                        CaptureRequest.JPEG_ORIENTATION,
                        (sensorOrientation!! - OREIENTATIONS.get(rotation) * (if (useCameraFront) 1 else -1) + 360) % 360
                )
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                if (flashSupported) {
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                }
            }

            val captureCallback = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureStarted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        timestamp: Long,
                        frameNumber: Long
                ) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber)
                }

                override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                ) {
                    Log.d(TAG, "capture onCaptureCompleted: ")
                    super.onCaptureCompleted(session, request, result)
                    unlockFocus()
                    Kpi.end(Kpi.TYPE.SHOT_TO_SHOT)
                    Log.d(TAG, "onCaptureCompleted: jpgImageQueue.size=${jpgImageQueue.size}")
                    val image = jpgImageQueue.take()
                    // clear the queue of images, if there are left
                    while (jpgImageQueue.size > 0) {
                        jpgImageQueue.take().close()
                    }
                    imageReaderHandler?.post(
                            ImageSaver(
                                    this@CameraActivity,
                                    image,
                                    imageReaderHandler
                            )
                    )
                }
            }

            currentCaptureSession?.apply {
//                stopRepeating()
//                abortCaptures()
                capture(captureBuilder.build(), captureCallback, cameraHandler)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        yuvLatestReceivedImage = null
    }

    // Lock the focus as the first step for a still image capture.
    private fun lockFocus() {
        Log.d(TAG, "lockFocus: ")
        try {
            // This is how to tell the camera to lock focus.
            previewRequestBuilder.set(
                    CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            // Tell #captureCallback to wait for the lock.
            cameraState = STATE_WAITING_LOCK
            currentCaptureSession?.capture(
                    previewRequestBuilder.build(), captureCallback,
                    cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    // Unlock the focus. This method should be called when still image capture sequence is finished.
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            previewRequestBuilder.apply {
                set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL)
                if (flashSupported) {
                    set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                }
            }
            currentCaptureSession?.capture(
                    previewRequestBuilder.build(), captureCallback,
                    cameraHandler
            )
            // After this, the camera will go back to the normal state of preview.
            cameraState = STATE_PREVIEW
            currentCaptureSession?.setRepeatingRequest(
                    previewRequest, captureCallback,
                    cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun viewPicture() {
        if (this::capturedImageUri.isInitialized) {
            val intent = Intent().apply {
                // action = Intent.ACTION_VIEW
                action = "com.android.camera.action.REVIEW"
                setDataAndType(capturedImageUri, "image/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else {
            toast("请先拍照")
        }
    }

    private fun updateThumbnail(capturedImageUri: Uri) {
        // scale animation from 1 - 1.2 - 1
        ivThumbnail.animate()
                .setDuration(80)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .withEndAction {
                    ivThumbnail.animate()
                            .setDuration(80)
                            .scaleX(1f)
                            .scaleY(1f)
                            .start()
                }
                .start()
        val thumbnail = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            val temp = MediaStore.Images.Media.getBitmap(contentResolver, capturedImageUri)
            ThumbnailUtils.extractThumbnail(temp, 100, 100)
        } else {
            contentResolver.loadThumbnail(capturedImageUri, Size(100, 100), null)
        }
        ivThumbnail.apply {
            post {
                Kpi.end(Kpi.TYPE.IMAGE_TO_THUMBNAIL)
                setImageBitmap(thumbnail)
            }
        }
    }

    private fun closePreviewSession() {
        currentCaptureSession?.close()
        currentCaptureSession = null
    }

    private fun startRecordingVideo() {
        if (cameraDevice == null) return
        try {
            closePreviewSession()
            setUpMediaRecorder()

            val recorderSurface = mediaRecorder!!.surface
            val surfaces = ArrayList<Surface>().apply {
                add(previewSurface)
                add(recorderSurface)
            }

            previewRequestBuilder =
                    cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                            .apply {
                                addTarget(previewSurface)
                                addTarget(recorderSurface)
                            }

            cameraDevice?.createCaptureSession(
                    surfaces,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(p0: CameraCaptureSession) {
                            currentCaptureSession = p0
                            updatePreview()
                            runOnUiThread {
                                ivRecord.setImageResource(R.drawable.btn_record_stop)
                                isRecordingVideo = true
                                mediaRecorder?.start()
                            }
                        }

                        override fun onConfigureFailed(p0: CameraCaptureSession) {
                            toast("Failed")
                        }
                    }, cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: IOException) {
            Log.e(TAG, e.toString())
        }
    }

    @Throws(IOException::class)
    private fun setUpMediaRecorder() {
        mediaRecorder = MediaRecorder()

        if (videoPath.isNullOrEmpty()) {
            videoPath = getVideoFilePath()
        }

        val rotation = windowManager.defaultDisplay.rotation
        when (sensorOrientation) {
            SENSOR_ORIENTATION_DEFAULT_DEGREES ->
                mediaRecorder?.setOrientationHint(OREIENTATIONS.get(rotation))

            SENSOR_ORIENTATION_INVERSE_DEGREES ->
                mediaRecorder?.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation))
        }
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoPath)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setVideoSize(videoSize.width, videoSize.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
        }
    }

    private fun stopRecordingVideo() {
        isRecordingVideo = false
        ivRecord.setImageResource(R.drawable.btn_record_start)
        mediaRecorder?.apply {
            stop()
            reset()
        }
        toast("Video saved: $videoPath")
        videoPath = null
        startCaptureSession()
    }

    private fun getVideoFilePath(): String {
        //        val filename = "${System.currentTimeMillis()}.mp4"
        val filename = "record.mp4"
        val dir = getExternalFilesDir(null)

        return if (dir == null) {
            filename
        } else {
            "${dir.absolutePath}/$filename"
        }
    }

    private fun handleRotation(rotateAngle: Int) {
        Log.d(TAG, "handleRotation: thumbnailOrientation:$thumbnailOrientation")
        thumbnailOrientation = (-rotateAngle + thumbnailOrientation +
                (if (rotateAngle > 180) 360 else 0)) % 360
        ivThumbnail.animate()
                .setDuration(800)
                .rotation(thumbnailOrientation.toFloat())
                .start()
        btnSwitchCamera.animate()
                .setDuration(800)
                .rotation(thumbnailOrientation.toFloat())
                .start()
    }

    companion object {
        private val TAG = CameraActivity::class.java.simpleName
        private val OREIENTATIONS = SparseIntArray()

        private const val MY_PERMISSIONS_REQUEST = 1001

        private const val YUV_IMAGE_READER_SIZE = 8
        private const val ZSL_IMAGE_WRITER_SIZE = 2
        private const val IMAGE_BUFFER_SIZE = 3

        private const val RecordMode = 0
        private const val CaptureMode = 1
        private const val BroadcastModel = 2
        private val cameraModes = arrayOf(
                "视频", "拍照", "主播"
        )

        init {
            OREIENTATIONS.append(Surface.ROTATION_0, 0)
            OREIENTATIONS.append(Surface.ROTATION_90, 90)
            OREIENTATIONS.append(Surface.ROTATION_180, 180)
            OREIENTATIONS.append(Surface.ROTATION_270, 270)

        }

        private const val SENSOR_ORIENTATION_DEFAULT_DEGREES = 90
        private const val SENSOR_ORIENTATION_INVERSE_DEGREES = 270

        private val INVERSE_ORIENTATIONS = SparseIntArray().apply {
            append(Surface.ROTATION_0, 270)
            append(Surface.ROTATION_90, 180)
            append(Surface.ROTATION_180, 90)
            append(Surface.ROTATION_270, 0)
        }

        /**
         * Camera state: Showing camera preview.
         */
        private val STATE_PREVIEW = 0

        /**
         * Camera state: Waiting for the focus to be locked.
         */
        private val STATE_WAITING_LOCK = 1

        /**
         * Camera state: Waiting for the exposure to be precapture state.
         */
        private val STATE_WAITING_PRECAPTURE = 2

        /**
         * Camera state: Waiting for the exposure state to be something other than precapture.
         */
        private val STATE_WAITING_NON_PRECAPTURE = 3

        /**
         * Camera state: Picture was taken.
         */
        private val STATE_PICTURE_TAKEN = 4
    }
}