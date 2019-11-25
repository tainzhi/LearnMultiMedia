package com.tainzhi.sample.media.opengl2.base

import android.content.Context
import android.opengl.GLSurfaceView
import java.util.jar.Attributes

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-23 11:48
 * @description:
 **/

open class BaseGLSurfaceView(context: Context?) : GLSurfaceView(context) {
    constructor(context: Context?, attributes: Attributes) : this(context) {
    }

    init {
        setEGLContextClientVersion(2)
    }
}