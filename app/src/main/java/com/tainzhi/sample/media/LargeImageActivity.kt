package com.tainzhi.sample.media

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.view.*
import android.widget.Scroller
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.io.IOException
import java.io.InputStream

class LargeImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val largeImageView = LargeView(this).apply {
            try {
                val inputStream = assets.open("large_image1.jpg");
                setImageStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        val constraintLayout = ConstraintLayout(this).apply {
            addView(largeImageView)
        }
        setContentView(constraintLayout)
    }
}

class LargeView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr),
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        ScaleGestureDetector.OnScaleGestureListener {

    // 图片的宽高
    private var imageWidth = 0f
    private var imageHeight = 0f
    // view's width height
    private var viewWidth = 0f
    private var viewHeight = 0f

    private var scale = 1f
    private var currentScale = 1f

    // 放大3倍
    private var multiple = 3

    private val rect = Rect()
    private var regionDecoder: BitmapRegionDecoder? = null
    private val options: BitmapFactory.Options = BitmapFactory.Options()
    private var bitmap: Bitmap? = null
    // 滑动器
    private val scroller = Scroller(context)

    private val _matrix = Matrix()
    // 手势识别器
    private val gestureDetector = GestureDetector(context, this)
    private val scaleGestureDetector = ScaleGestureDetector(context, this)

    fun setImageStream(inputStream: InputStream) {
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(inputStream, null, options)
        imageWidth = options.outWidth.toFloat()
        imageHeight = options.outHeight.toFloat()
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inJustDecodeBounds = false
        try {
            regionDecoder = BitmapRegionDecoder.newInstance(inputStream, false)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        requestLayout()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewWidth = w.toFloat()
        viewHeight = h.toFloat()
        rect.top = 0
        rect.left = 0
        rect.right = viewWidth.toInt()
        rect.bottom = viewHeight.toInt()
        scale = viewHeight / imageHeight
        currentScale = scale
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (regionDecoder == null) return

        // 复用内存
        options.inBitmap = bitmap
        bitmap = regionDecoder!!.decodeRegion(rect, options)
        _matrix.setScale(currentScale, currentScale)
        canvas?.drawBitmap(bitmap!!, _matrix, null)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        // 如果正在滑动, 先停止
        if (!scroller.isFinished) {
            scroller.forceFinished(true)
        }
        return true
    }

    override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
        scroller.fling(rect.left, rect.top, -velocityX.toInt(), -velocityY.toInt(), 0, imageWidth
                .toInt(), 0, imageHeight.toInt())
        return false
    }

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
        // rect.offset(distanceX.toInt(), distanceY.toInt())
        rect.offset(distanceX.toInt(), 0)
        handleBorder()
        invalidate()
        return false
    }

    private fun handleBorder() {
        // if (rect.left < 0) {
        //     rect.left = 0
        //     rect.right = (viewWidth / currentScale).toInt()
        // }
        // if (rect.right > imageWidth) {
        //     rect.right = imageWidth.toInt()
        //     rect.left = ((imageWidth - viewWidth) / currentScale).toInt()
        // }
        // if (rect.top < 0) {
        //     rect.top = 0
        //     rect.bottom = (viewHeight / currentScale).toInt()
        // }
        // if (rect.bottom > imageHeight) {
        //     rect.bottom = imageHeight.toInt()
        //     rect.top = ((imageHeight - viewHeight) / currentScale).toInt()
        // }
        if (rect.right > imageWidth) {
            rect.right = imageWidth.toInt()
        }
    }

    override fun computeScroll() {
        super.computeScroll()
        if (!scroller.isFinished && scroller.computeScrollOffset()) {
            if (rect.top + viewHeight / currentScale < imageHeight) {
                rect.top = scroller.currY
                rect.bottom = (rect.top + viewHeight / currentScale).toInt()
            }
            if (rect.bottom > imageHeight) {
                rect.top = (imageHeight - viewHeight / currentScale).toInt()
                rect.bottom = imageHeight.toInt()
            }
            invalidate()
        }
    }


    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onDoubleTap(e: MotionEvent?): Boolean {
        if (currentScale > scale) {
            currentScale = scale
        } else {
            currentScale = scale * multiple
        }
        rect.right = rect.left + (viewWidth / currentScale).toInt()
        rect.bottom = rect.top + (viewHeight / currentScale).toInt()

        handleBorder()
        invalidate()
        return true
    }

    override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
        return false
    }

    override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
        // 当 >= 2个手指触摸屏幕时调用, 若返回false则忽略事件调用
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector?) {
    }

    override fun onScale(detector: ScaleGestureDetector?): Boolean {
        val scaleFactor = detector!!.scaleFactor
        currentScale *= scaleFactor
        if (currentScale > scale * multiple) {
            currentScale = scale * multiple
        } else if (currentScale <= scale) {
            currentScale = scale
        }
        rect.right = rect.left + (viewWidth / currentScale).toInt()
        rect.bottom = rect.top + (viewHeight / currentScale).toInt()
        invalidate()
        return true
    }

}