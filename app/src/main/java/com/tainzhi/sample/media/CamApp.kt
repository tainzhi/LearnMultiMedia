package com.tainzhi.sample.media

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import com.tainzhi.sample.media.camera.CameraActivity
import com.tainzhi.sample.media.camera.gl.ShaderCache
import com.tainzhi.sample.media.camera.util.SettingsManager

class CamApp: Application(), ActivityLifecycleCallbacks {

    private lateinit var settingsManager: SettingsManager
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        registerActivityLifecycleCallbacks(this)
        SettingsManager.build(this)
        settingsManager = SettingsManager.getInstance()!!
        ShaderCache.load()
        System.loadLibrary("opencv")
        initOpenCV()
    }


    external fun initOpenCV()

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
        if (activity is CameraActivity) {
            ShaderCache.save()
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
    }

    companion object {
        @Volatile private lateinit var INSTANCE: CamApp
        fun getInstance() = INSTANCE

        val DEBUG = BuildConfig.DEBUG
    }

}