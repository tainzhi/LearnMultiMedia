package com.tainzhi.sample.media.opengl2.glsv

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import com.tainzhi.sample.media.camera.gl.BaseGLSurfaceView
import com.tainzhi.sample.media.opengl2.image.ImageRenderer

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/26 下午2:13
 * @description:
 **/

class ImageGLSurfaceView(context: Context, attributes: AttributeSet? = null) : BaseGLSurfaceView(context, attributes) {
    init {
        // setRenderer(ImageRenderer(context)) // 展示图片渲染器
        // setRenderer(ImageTransformRenderer(context, COOL));  //
        // 展示图片处理渲染器
        // renderMode = RENDERMODE_WHEN_DIRTY
        // requestRender()
    }
    
    var bitmap: Bitmap?= null
        set(value) {
            val render: ImageRenderer = ImageRenderer(context)
            render.mBitmap = value
            setRenderer(render)
            renderMode = RENDERMODE_WHEN_DIRTY
            field = value
        }
    //
    // var render: ImageRenderer = ImageRenderer(context)
    //     set(value) {
    //         setRenderer(render)
    //         field = value
    //     }
}