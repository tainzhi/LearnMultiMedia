package com.tainzhi.sample.media.camera

import android.util.Size
import java.lang.Long.signum

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/27 下午5:11
 * @description:
 **/

class CompareSizesByArea : Comparator<Size> {
    override fun compare(p0: Size, p1: Size) =
            signum(p0.width.toLong() * p0.height - p1.width.toLong() * p1.height)
}