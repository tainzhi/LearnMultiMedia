package com.tainzhi.sample.media.util

import android.app.Activity
import android.content.Context
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

fun Activity.toast(text: String) {
    runOnUiThread {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}


fun Context.toast(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}
