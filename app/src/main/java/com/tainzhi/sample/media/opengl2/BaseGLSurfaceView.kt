package com.tainzhi.sample.media.opengl2

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019-11-23 11:48
 * @description:
 **/

open class BaseGLSurfaceView(context: Context, attributes: AttributeSet? = null) : GLSurfaceView(context, attributes) {
    
    init {
        setEGLContextClientVersion(3)
    }
}