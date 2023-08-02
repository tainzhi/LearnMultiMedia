package com.tainzhi.sample.media.camera.util

import android.content.Context
import androidx.preference.PreferenceManager

class SettingsManager(context: Context) {
    private val sp = PreferenceManager.getDefaultSharedPreferences(context)
    private val spEditor = sp.edit()
    fun setBoolean(key: String, value: Boolean, defaultValue: Boolean = false) {
        spEditor.putBoolean(key, value)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sp.getBoolean(key, defaultValue)
    }
    fun commit() {
        spEditor.commit()
    }

    companion object {
        @Volatile private var INSTANCE: SettingsManager? = null
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
    }
    enum class PreviewRatio {
        RATIO_1x1,
        RATIO_3x4,
        RATIO_6x19,
        RATIO_FULL,
    }
}
