package com.tainzhi.sample.media.com.tainzhi.sample.media

import android.app.Application
import com.tainzhi.sample.media.tencent.matrix.battery.BatteryCanaryInitHelper
import com.tainzhi.sample.media.tencent.matrix.battery.BatteryCanarySimpleInitHelper
import com.tencent.matrix.Matrix
import com.tencent.matrix.batterycanary.BatteryEventDelegate

class MApp: Application() {
    override fun onCreate() {
        super.onCreate()
        initMatrix()
    }

    // reference: https://github.com/Tencent/matrix/blob/master/samples/sample-android/app/src/main/java/sample/tencent/matrix/MatrixApplication.java
    private fun initMatrix() {
        val matrixBuilder = Matrix.Builder(this)
        if (!BatteryEventDelegate.isInit()) {
            BatteryEventDelegate.init(applicationContext as Application)
        }

        matrixBuilder.plugin(BatteryCanaryInitHelper.createMonitor(this))
    // matrixBuilder.plugin(BatteryCanarySimpleInitHelper.createMonitor(this))

        Matrix.init(matrixBuilder.build())
    }
}