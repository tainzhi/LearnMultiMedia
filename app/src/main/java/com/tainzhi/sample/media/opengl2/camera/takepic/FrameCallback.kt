package com.tainzhi.sample.media.opengl2.camera.takepic

interface FrameCallback {
    fun onFrame(bytes: ByteArray?, time: Long)
}