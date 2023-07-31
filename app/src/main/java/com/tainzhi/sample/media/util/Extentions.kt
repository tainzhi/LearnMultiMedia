package com.tainzhi.sample.media.util

import android.util.Log

fun FloatArray.print(tag: String, name: String) {
    if (this.size != 16) {
        Log.w(tag, "Matrix-${name} is not a 4x4 matrix")
    }
    val result = StringBuffer("$name:\n")
    for (i in 0 until 16 step 4) {
        result.append("[${this[i]}, ${this[i + 1]}, ${this[i+2]}, ${this[i+3]}]\n")
    }
    Log.d(tag, result.toString())
}