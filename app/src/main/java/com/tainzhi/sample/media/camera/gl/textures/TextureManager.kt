package com.tainzhi.sample.media.camera.gl.textures

import android.graphics.RectF
import android.util.Log
import com.tainzhi.sample.media.camera.gl.ShaderFactory

class TextureManager {

    private val shaderFactory = ShaderFactory()
    private val textures = mutableListOf<TextureBase>()

    var previewRectF: RectF = RectF()
    private var isLoaded = false

    fun addTextures(textures: List<TextureBase>) {
        this.textures.addAll(textures)
    }

    // cannot be called in createContext() like unloadTextures()
    fun load() {
        Log.d(TAG, "load: ")
    }

    fun onDraw() {
        if (textures.size > 0 && !isLoaded) {
            shaderFactory.loadShaders()
            Log.d(TAG, "onDraw: loadShaders.size=${textures.size}")
            textures.forEach { it.load(shaderFactory, previewRectF) }
            isLoaded = true
        }
        if (isLoaded) {
            textures.forEach { it.draw() }
        }
    }

    fun unload() {
        Log.d(TAG, "unload: ")
        isLoaded = false
        textures.forEach {
            it.unload()
        }
        textures.clear()
        shaderFactory.clearShaders()
    }

    fun setMatrix(model: FloatArray, view: FloatArray, projection:FloatArray) {
        textures.forEach {
            it.setMatrix(model, view, projection)
        }

    }

    companion object {
        private val TAG = TextureManager::class.java.simpleName
    }
}