package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import com.tainzhi.sample.media.opengl2.base.BaseGLSurfaceView
import com.tainzhi.sample.media.opengl2.image.ImageRenderer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/26 下午2:13
 * @description:
 **/

class ImageGLSurfaceView(context: Context) : BaseGLSurfaceView(context) {
    init {
        setRenderer(ImageRenderer(context)) // 展示图片渲染器
//        setRenderer(ImageTransformRenderer(context, ImageTransformRenderer.Filter.MAGN));  // 展示图片处理渲染器
        renderMode = RENDERMODE_WHEN_DIRTY
        requestRender()
    }
}