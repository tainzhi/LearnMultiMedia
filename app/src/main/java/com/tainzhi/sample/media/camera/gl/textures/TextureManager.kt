package com.tainzhi.sample.media.camera.gl.textures

import android.graphics.RectF
import android.util.Log
import com.tainzhi.sample.media.camera.gl.ShaderFactory

class TextureManager {

    private val shaderFactory = ShaderFactory()
    private val textures = mutableListOf<TextureBase>(GridLine())

    var previewRectF: RectF = RectF()

    // cannot be called in createContext() like unloadTextures()
    fun loadTextures() {
        Log.d(TAG, "loadTextures: ")
    }

    fun onDraw() {
        if (!shaderFactory.isLoaded()) {
            shaderFactory.loadShaders()
            textures.forEach { it.load(shaderFactory, previewRectF) }
        }
        textures.forEach { it.draw() }
    }

    fun unloadTextures() {
        textures.forEach {
            it.unload()
        }
        shaderFactory.clearShaders()
    }

    fun setMatrix(model: FloatArray, view: FloatArray, projection:FloatArray) {
        Log.d(TAG, "setMatrix: ")
        textures.forEach {
            it.setMatrix(model, view, projection)
        }

    }

    companion object {
        private val TAG = TextureManager::class.java.simpleName
    }
}