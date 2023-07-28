package com.tainzhi.sample.media.camera

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.MediaRecorder
import android.util.Log
import android.util.Size
import java.util.Collections

class CameraInfoCache(cameraManager: CameraManager, useFrontCamera: Boolean = false) {
    private lateinit var cameraCharacteristics: CameraCharacteristics
    var cameraId: String = ""
    var largestJpgSize =  Size(0, 0)
    var largestYuvSize = Size(0, 0)
    var videoSize = Size(0, 0)
    var isflashSupported = false
    private var requestAvailableAbilities: IntArray? = null
    var sensorOrientation: Int? = 0
    private var noiseModes: IntArray? = null
    private var edgeModes: IntArray? = null
    private var streamConfigurationMap: StreamConfigurationMap? = null
    private var hardwareLevel: Int = 0
    var reprocessingNoiseMode = CameraCharacteristics.NOISE_REDUCTION_MODE_HIGH_QUALITY
    var reprocessingEdgeMode = CameraCharacteristics.EDGE_MODE_HIGH_QUALITY
    init {
        val cameraList = cameraManager.cameraIdList
        for (id in cameraList) {
            cameraCharacteristics = cameraManager.getCameraCharacteristics(id)
            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == (if (useFrontCamera) CameraMetadata.LENS_FACING_FRONT else CameraMetadata.LENS_FACING_BACK)) {
                cameraId = id
                break
            }
        }
        if (cameraCharacteristics == null) {
            throw Exception("cannot get camera characteristics")
        }
        streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        videoSize = chooseVideoSize(streamConfigurationMap!!.getOutputSizes(MediaRecorder::class.java))
        if (streamConfigurationMap == null) {
            throw Exception("cannot get stream configuration")
        }
        streamConfigurationMap?.outputFormats?.forEach {
            when(it) {
                ImageFormat.YUV_420_888 ->{
                    largestYuvSize = getLargestSize(streamConfigurationMap!!.getOutputSizes(ImageFormat.YUV_420_888))
                }
                ImageFormat.JPEG -> {
                    largestJpgSize = getLargestSize(streamConfigurationMap!!.getOutputSizes(ImageFormat.JPEG))
                }
            }
        }
        requestAvailableAbilities = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        edgeModes = cameraCharacteristics.get(CameraCharacteristics.EDGE_AVAILABLE_EDGE_MODES)
        noiseModes = cameraCharacteristics.get(CameraCharacteristics.NOISE_REDUCTION_AVAILABLE_NOISE_REDUCTION_MODES)
        hardwareLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)!!
        sensorOrientation = cameraCharacteristics!!.get(CameraCharacteristics.SENSOR_ORIENTATION)
        isflashSupported = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    }


    fun isSupportReproc(): Boolean {
        if (requestAvailableAbilities!= null &&
            requestAvailableAbilities!!.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING)  ||
            requestAvailableAbilities!!.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_PRIVATE_REPROCESSING))
        {
            return true
        }
        return false
    }

    fun getPreviewSurfaceSize(): Array<Size> {
        return streamConfigurationMap!!.getOutputSizes(SurfaceTexture::class.java)
    }

    fun isCamera2FullModeAvailable() = isHardwareLevelAtLeast(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL)

    fun getCaptureNoiseMode(): Int {
        if (noiseModes!!.contains(CameraCharacteristics.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG)) {
            return CameraCharacteristics.NOISE_REDUCTION_MODE_ZERO_SHUTTER_LAG
        } else {
            return CameraCharacteristics.NOISE_REDUCTION_MODE_FAST
        }
    }

    fun getEdgeMode(): Int {
        if (edgeModes!!.contains(CameraCharacteristics.EDGE_MODE_ZERO_SHUTTER_LAG)) {
            return CameraCharacteristics.EDGE_MODE_ZERO_SHUTTER_LAG
        } else {
            return CameraCharacteristics.EDGE_MODE_FAST
        }
    }
    private fun isHardwareLevelAtLeast(level: Int): Boolean {
        if (level == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) return true
        if (hardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) return false
        return hardwareLevel >= level;
    }

    companion object {

        fun getLargestSize(sizes: Array<Size>): Size {
            var largetstSize = Size(0, 0)
            var lartestArea = 0
            sizes.forEach {
                val tempArea = it.width * it.height
                if (tempArea > lartestArea) {
                    lartestArea = tempArea
                    largetstSize = it
                }
            }
            return largetstSize
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
        fun chooseOptimalSize(
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

        private val TAG = CameraInfoCache::class.java.simpleName
    }
}