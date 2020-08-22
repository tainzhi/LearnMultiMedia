package com.tainzhi.sample.media.com.tainzhi.sample.media.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.tainzhi.sample.media.com.tainzhi.sample.media.util.dp

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/8/22 22:57
 * @description:
 **/

class FaceView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var paint: Paint
    
    // 人脸识别框颜色
    private val color = "#42ed45"
    var faces: ArrayList<RectF>? = null
        set(value) {
            field = value
            invalidate()
        }
    
    init {
        paint = Paint().apply {
            this.color = Color.parseColor(this@FaceView.color)
            style = Paint.Style.STROKE
            strokeWidth = 1.dp()
            isAntiAlias = true
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 多个人脸需要绘制
        faces?.let {
            it.forEach {
                canvas.drawRect(it, paint)
            }
        }
    }
}