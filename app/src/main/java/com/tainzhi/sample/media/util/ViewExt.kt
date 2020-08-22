package com.tainzhi.sample.media.com.tainzhi.sample.media.util

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/7/20 13:59
 * @description:
 **/


//dp转px
fun Int.dp(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics)
}

//dp转px
fun Float.dp(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)
}

//sp转px
fun Int.sp(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics)
}

//sp转px
fun Float.sp(): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)
}
