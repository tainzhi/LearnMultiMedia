package com.tainzhi.sample.media.camera

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.tainzhi.sample.media.R
import com.tainzhi.sample.media.databinding.ActivityCameraBinding
import com.tainzhi.sample.media.util.toast
import com.tainzhi.sample.media.widget.AutoFitTextureView
import com.tainzhi.sample.media.widget.CircleImageView
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
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

class CameraActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var rootView: View
    private lateinit var _binding: ActivityCameraBinding

    private val permissions_exclude_storage = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
    )
    private val permission_storage = Manifest.permission.WRITE_EXTERNAL_STORAGE

    private val unGrantedPermissionList: MutableList<String> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        rootView = _binding.root

        setFullScreen()

        findViewById<View>(R.id.picture).setOnClickListener(this)
        findViewById<View>(R.id.iv_preview).setOnClickListener(this)
        findViewById<View>(R.id.iv_record).setOnClickListener(this)
        findViewById<View>(R.id.iv_change_camera).setOnClickListener(this)
        textureView = findViewById(R.id.texture)
        picturePreview = findViewById(R.id.iv_preview)
        ivTakePicture = findViewById(R.id.picture)
        ivRecord = findViewById(R.id.iv_record)

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

        checkPermissions()
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

    private lateinit var textureView: AutoFitTextureView

    // 预览拍照的图片，用于相册打开
    private lateinit var picturePreview: CircleImageView
    private lateinit var ivTakePicture: ImageView
    private lateinit var ivRecord: ImageView

    private lateinit var capturedImageUri: Uri

    private var surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, width: Int, height: Int) {
            configureTransform(width, height)
        }

        override fun onSurfaceTextureUpdated(p0: SurfaceTexture) = Unit

        override fun onSurfaceTextureDestroyed(p0: SurfaceTexture) = true

        override fun onSurfaceTextureAvailable(p0: SurfaceTexture, width: Int, height: Int) {
            openCamera(width, height)
        }
    }

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private var cameraHandler = Handler(cameraThread.looper)

    private var imageReaderThread = HandlerThread("ImageReaderThread").apply { start() }
    private val imageReaderHandler = Handler(imageReaderThread.looper) { msg ->
        when (msg.what) {
            CAMERA_UPDATE_PREVIEW_PICTURE -> {
                val filePath: String = msg.obj as String
                updatePreviewPicture(filePath)
            }
        }
        false
    }

    // 用于子线程给主线程通信
    private var mainHandler: Handler? = null

    // a [Semaphore] to prevent the app from exiting before closing the camera
    private val cameraOpenCloseLock = Semaphore(1)

    private lateinit var cameraId: String

    // for camera preview
    private var captureSession: CameraCaptureSession? = null
    private var cameraDevice: CameraDevice? = null
    private lateinit var previewSize: Size

    // [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.
    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(p0: CameraDevice) {
            cameraOpenCloseLock.release()
            this@CameraActivity.cameraDevice = p0
            startPreview()
        }

        override fun onDisconnected(p0: CameraDevice) {
            super.onClosed(p0)
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

    private var flashSupported = false

    // orientation of the camera sensor
    private var sensorOrientation = 0

    private var cameraState = STATE_PREVIEW

    private var camera = BACK_CAMERA

    // handles still image capture
    private lateinit var imageReader: ImageReader

    private var isRecordingVideo = false
    private var mediaRecorder: MediaRecorder? = null
    private var videoPath: String? = null
    private lateinit var videoSize: Size

    /**
     * [CaptureRequest.Builder] for the camera preview
     */
    private lateinit var previewRequestBuilder: CaptureRequest.Builder

    /**
     * [CaptureRequest] generated by [.previewRequestBuilder]
     */
    private lateinit var previewRequest: CaptureRequest


    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        private fun process(result: CaptureResult) {
            when (cameraState) {
                STATE_PREVIEW -> Unit // Do nothing when the camera preview is working normally.
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
                        captureStillPicture()
                    }
                }
            }
        }

        private fun capturePicture(result: CaptureResult) {
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)
            if (afState == null) {
                captureStillPicture()
            } else if (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                || afState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED
            ) {
                // CONTROL_AE_STATE can be null on some devices
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

    override fun onResume() {
        Log.i(TAG, "onResume: ")
        super.onResume()
        startBackgroundThread()
        if (textureView.isAvailable) {
            openCamera(textureView.width, textureView.height)
        } else {
            textureView.surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        super.onPause()
        stopBackgroundThread()
        closeCamera()
        Log.i(TAG, "onPause: ")
    }

    private fun openCamera(width: Int, height: Int) {
        setUpCameraOutputs(width, height)
        configureTransform(width, height)
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
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
            manager.openCamera(cameraId, stateCallback, cameraHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    private fun closeCamera() {
        try {
            cameraOpenCloseLock.acquire()
            captureSession?.close()
            captureSession = null
            cameraDevice?.close()
            cameraDevice = null
            imageReader?.close()
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    private fun startBackgroundThread() {
    }

    private fun stopBackgroundThread() {
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
//        try {
//            cameraThread?.join()
//            cameraThread = null
//            cameraHandler = null
//            mainHandler = null
//        } catch (e: InterruptedException) {
//            Log.e(TAG, e.toString())
//        }
    }

    /**
     * Sets up member variables related to camera.
     *
     * @param width  The width of available size for camera preview
     * @param height The height of available size for camera preview
     */
    private fun setUpCameraOutputs(width: Int, height: Int) {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_FRONT &&
                    camera == BACK_CAMERA
                ) {
                    continue
                } else if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_BACK &&
                    camera == FRONT_CAMERA
                ) {
                    continue
                }

                val map = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
                ) ?: continue

                videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder::class.java))
                val largest = Collections.max(
                    Arrays.asList(*map.getOutputSizes(ImageFormat.JPEG)),
                    CompareSizesByArea()
                )
                imageReader = ImageReader.newInstance(
                    largest.width, largest.height,
                    ImageFormat.JPEG, IMAGE_BUFFER_SIZE
                )

                val displayRotation = windowManager?.defaultDisplay?.rotation
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                    ?: 0

                val swappedDimensions = areDimensionsSwapped(displayRotation)

                val displaySize = Point()
                windowManager?.defaultDisplay?.getSize(displaySize)
                val rotatedPreviewWidth = if (swappedDimensions) height else width
                val rotatedPreviewHeight = if (swappedDimensions) width else height
                var maxPreviewWidth = if (swappedDimensions) displaySize.y else displaySize.x
                var maxPreviewHeight = if (swappedDimensions) displaySize.x else displaySize.y
//                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) maxPreviewWidth = MAX_PREVIEW_WIDTH
//                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) maxPreviewHeight = MAX_PREVIEW_HEIGHT

                // Danger, W.R.! Attempting to use too large a preview size could exceed the camera
                // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
                // garbage capture data.
                previewSize = chooseOptimalSize(
                    map.getOutputSizes(SurfaceTexture::class.java),
                    rotatedPreviewWidth, rotatedPreviewHeight,
                    maxPreviewWidth, maxPreviewHeight,
                    largest
                )
                if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    textureView.setAspectRatio(previewSize.width, previewSize.height)
                } else {
                    textureView.setAspectRatio(previewSize.height, previewSize.width)
                }

                flashSupported =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                this@CameraActivity.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.camera_error))
                .show(supportFragmentManager, "fragment_dialog")
        }
    }


    /**
     * Determines if the dimensions are swapped given the phone's current rotation.
     *
     * @param displayRotation The current rotation of the display
     *
     * @return true if the dimensions are swapped, false otherwise.
     */
    private fun areDimensionsSwapped(displayRotation: Int?): Boolean {
        var swappedDimensions = false
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> {
                if (sensorOrientation == 90 || sensorOrientation == 270) {
                    swappedDimensions = true
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

    private fun startPreview() {
        try {
            val texture = textureView.surfaceTexture

            texture?.setDefaultBufferSize(previewSize.width, previewSize.height)

            val surface = Surface(texture)

            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder.addTarget(surface)

            cameraDevice?.createCaptureSession(
                Arrays.asList(surface, imageReader?.surface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigureFailed(p0: CameraCaptureSession) {
                        toast("Failed")
                    }

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        updatePreview()
                    }
                }, cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun updatePreview() {
        if (cameraDevice == null) return


        try {
            // Auto focus should be continuous for camera preview.
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            setAutoFlash(previewRequestBuilder)

            previewRequest = previewRequestBuilder.build()
            captureSession?.setRepeatingRequest(
                previewRequest,
                captureCallback, cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }


    /**
     * Configures the necessary [android.graphics.Matrix] transformation to `textureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `textureView` is fixed.
     *
     * @param viewWidth  The width of `textureView`
     * @param viewHeight The height of `textureView`
     */
    private fun configureTransform(viewWidth: Int, viewHeight: Int) {
        val rotation = windowManager?.defaultDisplay?.rotation
        val matrix = Matrix()
        val viewRect = RectF(0f, 0f, viewWidth.toFloat(), viewHeight.toFloat())
        val bufferRect = RectF(0f, 0f, previewSize.height.toFloat(), previewSize.width.toFloat())
        val centerX = viewRect.centerX()
        val centerY = viewRect.centerY()

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY())
            val scale = Math.max(
                viewHeight.toFloat() / previewSize.height,
                viewWidth.toFloat() / previewSize.width
            )
            with(matrix) {
                setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL)
                postScale(scale, scale, centerX, centerY)
                postRotate((90 * (rotation - 2)).toFloat(), centerX, centerY)
            }
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180f, centerX, centerY)
        }
        textureView.setTransform(matrix)
    }

    /**
     * Lock the focus as the first step for a still image capture.
     */
    private fun lockFocus() {
        try {
            // This is how to tell the camera to lock focus.
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START
            )
            // Tell #captureCallback to wait for the lock.
            cameraState = STATE_WAITING_LOCK
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                cameraHandler
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
            captureSession?.capture(
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
        try {
            // flush any images left in the image queue
            while (imageReader.acquireLatestImage() != null) {
            }
            val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
            imageReader.setOnImageAvailableListener({ reader ->
                val image = reader.acquireNextImage()
                imageQueue.add(image)
                Log.d(TAG, "captureStillPicture: image available in queue: ${image.timestamp}")
                // val data = YUVTool.getBytesFromImageReader(it)
                // val myMediaRecorder =  MyMediaRecorder()
                // myMediaRecorder.addVideoData(data)
            }, imageReaderHandler)
            if (cameraDevice == null) return
            val rotation = windowManager.defaultDisplay.rotation

            val captureBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE
            ).apply {
                addTarget(imageReader.surface)
                // set(CaptureRequest.JPEG_ORIENTATION, (OREIENTATIONS.get(rotation) + sensorOrientation + 270) % 360)
                set(CaptureRequest.JPEG_ORIENTATION, OREIENTATIONS.get(rotation))
                set(
                    CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                )
            }.also { setAutoFlash(it) }

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
                    super.onCaptureCompleted(session, request, result)
                    unlockFocus()

                    imageReaderHandler?.post(ImageSaver(this@CameraActivity, imageQueue, mainHandler))
                    imageReader.setOnImageAvailableListener(null, null)
                }
            }

            captureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureBuilder.build(), captureCallback, null)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    /**
     * Unlock the focus. This method should be called when still image capture sequence is
     * finished.
     */
    private fun unlockFocus() {
        try {
            // Reset the auto-focus trigger
            previewRequestBuilder.set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_CANCEL
            )
            setAutoFlash(previewRequestBuilder)
            captureSession?.capture(
                previewRequestBuilder.build(), captureCallback,
                cameraHandler
            )
            // After this, the camera will go back to the normal state of preview.
            cameraState = STATE_PREVIEW
            captureSession?.setRepeatingRequest(
                previewRequest, captureCallback,
                cameraHandler
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }

    }

    override fun onClick(p0: View?) {
        when (p0?.id) {
            R.id.picture -> lockFocus()
            R.id.iv_record -> if (isRecordingVideo) stopRecordingVideo() else startRecordingVideo()
            R.id.iv_preview -> viewPicture()
            R.id.iv_change_camera -> {
                closeCamera()
                if (textureView.isAvailable) {
                    camera = if (camera == FRONT_CAMERA) BACK_CAMERA else FRONT_CAMERA
                    openCamera(textureView.width, textureView.height)
                }
            }
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    private fun viewPicture() {
        // try {
        //     val intent = Intent().apply {
        //         action = Intent.ACTION_VIEW
        //         setDataAndType(fileUri, "image/*")
        //         flags = Intent.FLAG_ACTIVITY_NEW_TASK
        //     }
        //     startActivity(intent)
        //
        //     // 打开系统相册或者系统文件夹, 前提是文件已经保存到系统MediaStore中
        //     // startActivityForResult(Intent(Intent.ACTION_PICK, fileUri).apply {
        //     //     type = "image/*"
        //     // },
        //     //         1000
        //     // )
        // } catch (e: Exception) {
        //     Log.e("Camemra", e.message)
        // }
        if (this::capturedImageUri.isInitialized) {
            val intent = Intent().apply {
                action = Intent.ACTION_VIEW
                setDataAndType(capturedImageUri, "image/*")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } else {
            toast("请先拍照")
        }
    }

    private fun updatePreviewPicture(picPath: String) {
        capturedImageUri = Uri.parse(picPath)
        val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getBitmap(contentResolver, capturedImageUri)
        } else {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(
                    contentResolver,
                    capturedImageUri
                )
            )
        }
        val thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 100, 100)
        // val thumbnail = getThumbnail(requireContext(), capturedImageUri)
        picturePreview.apply {
            post {
                setImageBitmap(thumbnail)
            }
        }
    }

    private fun closePreviewSession() {
        captureSession?.close()
        captureSession = null
    }

    private fun startRecordingVideo() {
        if (cameraDevice == null || !textureView.isAvailable) return
        try {
            closePreviewSession()
            setUpMediaRecorder()
            val texture = textureView.surfaceTexture?.apply {
                setDefaultBufferSize(previewSize.width, previewSize.height)
            }

            val previewSurface = Surface(texture)
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
                        captureSession = p0
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
        startPreview()
    }


    /**
     * In this sample, we choose a video size with 3x4 aspect ratio. Also, we don't use sizes
     * larger than 1080p, since MediaRecorder cannot handle such a high-resolution video.
     *
     * @param choices The list of available sizes
     * @return The video size
     */
    private fun chooseVideoSize(choices: Array<Size>) = choices.firstOrNull {
        it.width == it.height * 4 / 3 && it.width <= 1080
    } ?: choices[choices.size - 1]

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

//    private fun checkPermissions() {
//        for (permission in permissions) {
//            if (ContextCompat.checkSelfPermission(activity as Context, permission) != PackageManager
//                            .PERMISSION_GRANTED) {
//                unGrantedPermissionList.add(permission)
//            }
//        }
//        val arrayString = arrayOfNulls<String>(unGrantedPermissionList.size)
//        unGrantedPermissionList.toArray(arrayString)
//        if (unGrantedPermissionList.isNotEmpty()) {
//            ActivityCompat.requestPermissions(activity as Activity,
//                    arrayString,
//                    10001)
//        }
//    }


    companion object {
        private const val TAG = "CameraActivity"
        private val OREIENTATIONS = SparseIntArray()

        private const val MY_PERMISSIONS_REQUEST = 1001

        private const val IMAGE_BUFFER_SIZE = 3

        private const val RecordMode = 0
        private const val CaptureMode = 1
        private const val BroadcastModel = 2
        private val cameraModes = arrayOf(
            "视频", "拍照", "主播"
        )

        init {
            OREIENTATIONS.append(Surface.ROTATION_0, 90)
            OREIENTATIONS.append(Surface.ROTATION_90, 0)
            OREIENTATIONS.append(Surface.ROTATION_180, 270)
            OREIENTATIONS.append(Surface.ROTATION_270, 180)

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

        /**
         * Max preview width that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_WIDTH = 1920

        /**
         * Max preview height that is guaranteed by Camera2 API
         */
        private val MAX_PREVIEW_HEIGHT = 1080

        private val FRONT_CAMERA = 0
        private val BACK_CAMERA = 1

        /**
         * Given `choices` of `Size`s supported by a camera, choose the smallest one that
         * is at least as large as the respective texture view size, and that is at most as large as
         * the respective max size, and whose aspect ratio matches with the specified value. If such
         * size doesn't exist, choose the largest one that is at most as large as the respective max
         * size, and whose aspect ratio matches with the specified value.
         *
         * @param choices           The list of sizes that the camera supports for the intended
         *                          output class
         * @param textureViewWidth  The width of the texture view relative to sensor coordinate
         * @param textureViewHeight The height of the texture view relative to sensor coordinate
         * @param maxWidth          The maximum width that can be chosen
         * @param maxHeight         The maximum height that can be chosen
         * @param aspectRatio       The aspect ratio
         * @return The optimal `Size`, or an arbitrary one if none were big enough
         */
        @JvmStatic
        private fun chooseOptimalSize(
            choices: Array<Size>,
            textureViewWidth: Int,
            textureViewHeight: Int,
            maxWidth: Int,
            maxHeight: Int,
            aspectRatio: Size
        ): Size {

            // Collect the supported resolutions that are at least as big as the preview Surface
            val bigEnough = ArrayList<Size>()
            // Collect the supported resolutions that are smaller than the preview Surface
            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            for (option in choices) {
                if (option.width <= maxWidth && option.height <= maxHeight &&
                    option.height == option.width * textureViewHeight / textureViewWidth
                ) {
                    if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                        bigEnough.add(option)
                    } else {
                        notBigEnough.add(option)
                    }
                }
            }

            // Pick the smallest of those big enough. If there is no one big enough, pick the
            // largest of those not big enough.
            if (bigEnough.size > 0) {
                return Collections.min(bigEnough, CompareSizesByArea())
            } else if (notBigEnough.size > 0) {
                return Collections.max(notBigEnough, CompareSizesByArea())
            } else {
                Log.e(TAG, "Couldn't find any suitable preview size")
                return choices[0]
            }
        }

        @Throws(FileNotFoundException::class, IOException::class)
        fun getThumbnail(context: Context, uri: Uri): Bitmap? {
            val THUMBNAIL_SIZE = 256
            var input: InputStream? = context.contentResolver.openInputStream(uri)
            val onlyBoundsOptions: BitmapFactory.Options = BitmapFactory.Options()
            onlyBoundsOptions.inJustDecodeBounds = true
            onlyBoundsOptions.inDither = true //optional
            onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //optional
            BitmapFactory.decodeStream(input, null, onlyBoundsOptions)
            input?.close()
            if (onlyBoundsOptions.outWidth === -1 || onlyBoundsOptions.outHeight === -1) {
                return null
            }
            val originalSize =
                if (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) onlyBoundsOptions.outHeight else onlyBoundsOptions.outWidth
            val ratio: Double =
                if (originalSize > THUMBNAIL_SIZE) originalSize.toDouble() / THUMBNAIL_SIZE else 1.0
            val bitmapOptions: BitmapFactory.Options = BitmapFactory.Options()
            bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio)
            bitmapOptions.inDither = true //optional
            bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888 //
            input = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions)
            input?.close()
            return bitmap
        }

        private fun getPowerOfTwoForSampleRatio(ratio: Double): Int {
            val k = Integer.highestOneBit(Math.floor(ratio).toInt())
            return if (k == 0) 1 else k
        }
    }
}