package com.tainzhi.sample.media.camera

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.params.StreamConfigurationMap
import android.util.Log
import android.util.Size
import java.lang.Long.signum
import java.util.Collections
import kotlin.math.abs

class CameraInfoCache(cameraManager: CameraManager, useFrontCamera: Boolean = false) {
    private lateinit var cameraCharacteristics: CameraCharacteristics
    var cameraId: String = ""
    var largestJpgSize =  Size(0, 0)
    var largestYuvSize = Size(0, 0)
    // to fixme
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
        // videoSize = chooseVideoSize(streamConfigurationMap!!.getOutputSizes(MediaRecorder::class.java))
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

    fun getOutputPreviewSurfaceSize(): Array<Size> {
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
         * @param viewSize 预览区域所在的窗口的大小，默认为整个屏幕大小，且不会改变。Portrait方向时，width < height转换下，
         *                  landscape方向时，width > height 无需转换
         *                  确保 viewSize.width > view.height
         * @param aspectRatio 预览区域的宽高比 w:h，确保 w > h 恒成立，所以该值恒大于等于1
         *      比如 1:1, 4:3, 16:9, full=device最长的边: device的短边
         */
        @JvmStatic
        fun chooseOptimalSize(
            choices: Array<Size>,
            viewSize: Size,
            aspectRatio: Float
        ): Pair<Size, Float> {
            Log.d(TAG, "chooseOptimalSize: viewSize=${viewSize}, aspectRatio=${aspectRatio}")
            val choosedSizes = ArrayList<Size>()
            for (option in choices) {
                Log.d(TAG, "chooseOptimalSize: $option")
                val tempRatio = option.width/option.height.toFloat()
                if (abs(aspectRatio - tempRatio) < DIFF_FLOAT_EPS) {
                    choosedSizes.add(option)
                }
            }
            // 首先选取宽高和预览窗口一直且最大的输出尺寸
            if (choosedSizes.size > 0) {
                Log.d(TAG, "optimal preview size by same w/h aspect ratio")
                val result = Collections.min(choosedSizes, CompareSizesByArea())
                return Pair(result, aspectRatio)
            }

            var suboptimalSize = Size(0, 0)
            var suboptimalAspectRatio = 1f
            // 如果不存在宽高比与预览窗口一致的输出尺寸，则选择与其宽高最接近的尺寸
            var minRatioDiff = Float.MAX_VALUE
            choices.forEach { option ->
                val tempRatio = option.width/option.height.toFloat()
                if (abs(tempRatio - aspectRatio) < minRatioDiff) {
                    minRatioDiff = abs(tempRatio - aspectRatio)
                    suboptimalAspectRatio = tempRatio
                    suboptimalSize = option
                }
            }
            if (suboptimalSize != Size(0, 0)) {
                Log.d(TAG, "optimal preview size by closet w/h aspect ratio")
                return Pair(suboptimalSize, suboptimalAspectRatio)
            }

            // 选择面积与预览窗口最接近的输出尺寸
            var minAreaDiff = Long.MAX_VALUE
            val previewArea = viewSize.height * aspectRatio * viewSize.height
            choices.forEach { option ->
                val tempArea = option.width * option.height
                if (abs(previewArea - tempArea) < minAreaDiff) {
                    suboptimalAspectRatio = option.width / option.height.toFloat()
                    suboptimalSize = option
                }
            }
            Log.d(TAG, "optimal preview size by choset area")
            return Pair(suboptimalSize, suboptimalAspectRatio)
        }

        private val TAG = CameraInfoCache::class.java.simpleName
        private const val DIFF_FLOAT_EPS = 0.0001f
    }
}

class CompareSizesByArea : Comparator<Size> {
    override fun compare(p0: Size, p1: Size) =
        signum(p0.width * p0.height - p1.width.toLong() * p1.height)
}
