package com.tainzhi.sample.media.camera.util

import android.content.Context
import android.net.Uri
import androidx.preference.PreferenceManager
import com.tainzhi.sample.media.R

class SettingsManager(val context: Context) {
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)
    private val spEditor = sp.edit()
    fun setBoolean(key: String, value: Boolean, defaultValue: Boolean = false) {
        spEditor.putBoolean(key, value)
        commit()
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sp.getBoolean(key, defaultValue)
    }
    fun commit() {
        spEditor.commit()
    }

    fun getPreviewAspectRatio(): PreviewAspectRatio {
        val type = sp.getInt(KEY_PREVIEW_RATIO, PREVIEW_RATIO_DEFAULT_VALUE.ordinal)
        return PreviewAspectRatio.values()[type]
    }

    fun setPreviewRatio(ratio: PreviewAspectRatio) {
        spEditor.putInt(KEY_PREVIEW_RATIO, ratio.ordinal)
        commit()
    }

    fun getGridLineEnable(): Boolean {
        return sp.getBoolean(context.getString(R.string.settings_key_enable_grid_line), true)
    }

    fun getGridLineType(): Int {
        return sp.getString(context.getString(R.string.settings_key_grid_line_option), null)?.toInt()
            ?:GridLineType.GOLDEN_SPIRAL.ordinal
    }

    fun getLastCapturedMediaUri(): Uri? {
        val uriString =  sp.getString(KEY_LAST_CAPTURED_MEDIA_URI, null)
        if (uriString != null) {
            return Uri.parse(uriString)
        } else {
            return null
        }
    }

    fun saveLastCaptureMediaUri(uri: Uri) {
        spEditor.putString(KEY_LAST_CAPTURED_MEDIA_URI, uri.toString())
        spEditor.commit()
    }

    companion object {
        @Volatile private lateinit var INSTANCE: SettingsManager
        fun build(context: Context) {
            INSTANCE = SettingsManager(context)
        }

        fun getInstance() = INSTANCE

        // more fast than disableZSL
        // e.g
        // disableZSL 450ms
        // enableZSL 278ms
        val KEY_PHOTO_ZSL = "photo_zsl"
        val PHOTO_ZSL_DEFAULT_VALUE = true
        val KEY_PREVIEW_RATIO = "preview_ratio"
        val KEY_HDR_ENABLE = "key_hdr_enable"
        val KEY_LAST_CAPTURED_MEDIA_URI = "key_last_captured_media_uri"
        val PREVIEW_RATIO_DEFAULT_VALUE = PreviewAspectRatio.RATIO_FULL
        val GRID_TYPE_DEFAULT_VALUE = GridLineType.GOLDEN_SPIRAL
    }
    enum class PreviewAspectRatio {
        RATIO_1x1,
        RATIO_4x3,
        RATIO_16x9,
        RATIO_FULL,
    }

    enum class GridLineType {
        DIAGONAL,
        CROSSHAIR_2X2,
        CONTOUR_3x3,
        GOLDEN_SECTION_3x3,
        GOLDEN_SPIRAL,
    }

    enum class FilterType {
        ORIGINAL,
        LUT_AMATORKA,
        LUT_HIGHKEY,
        LUT_PURITY,
        POSTERIZATION,
    }
}
