package com.tainzhi.sample.media.camera.gl.textures

import android.util.Log

class TextureManager {

    fun loadTextures() {
        Log.d(TAG, "loadTextures: ")
    }

    fun unloadTextures() {
        Log.d(TAG, "unloadTextures: ")
    }

    companion object {
        private val TAG = TextureManager::class.java.simpleName
    }
}