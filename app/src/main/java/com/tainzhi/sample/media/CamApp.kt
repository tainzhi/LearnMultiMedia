package com.tainzhi.sample.media

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.SharedPreferences
import android.os.Bundle
import com.tainzhi.sample.media.camera.util.SettingsManager

class CamApp: Application(), ActivityLifecycleCallbacks {

    private lateinit var sp : SharedPreferences
    private lateinit var settingsManager: SettingsManager
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        registerActivityLifecycleCallbacks(this)
        SettingsManager.build(this)
        settingsManager = SettingsManager.getInstance()!!
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
        settingsManager.commit()
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    companion object {
        lateinit var INSTANCE: CamApp
    }

}