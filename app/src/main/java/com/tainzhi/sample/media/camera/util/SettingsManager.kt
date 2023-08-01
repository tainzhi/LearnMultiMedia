package com.tainzhi.sample.media.camera.util

object SettingsManager {
    // more fast than disableZSL
    // e.g
    // disableZSL 450ms
    // enableZSL 278ms
    val KEY_PHOTO_ZSL = "photo_zsl"
    val PHOTO_ZSL_DEFAULT_VALUE = true
    enum class PreviewRatio {
        RATIO_1x1,
        RATIO_3x4,
        RATIO_6x19,
        RATIO_FULL,
    }
}
