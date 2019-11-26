package com.tainzhi.sample.media.opengl2.camera.takepic

import android.opengl.GLSurfaceView

/**
 * @自定义GLSurfaceView.Renderer@
 * @继承GLSurfaceView的渲染器，并增加onDestroy方法定义@
 */
interface Renderer : GLSurfaceView.Renderer {
    fun onDestroy()
}