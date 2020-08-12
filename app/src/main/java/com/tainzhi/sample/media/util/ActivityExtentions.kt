package com.tainzhi.sample.media.util

import android.widget.Toast
import androidx.fragment.app.FragmentActivity

/**
 * @author:       tainzhi
 * @mail:         qfq61@qq.com
 * @date:         2019/11/27 下午3:35
 * @description:
 **/
fun FragmentActivity.toast(text: String) {
    runOnUiThread {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}

