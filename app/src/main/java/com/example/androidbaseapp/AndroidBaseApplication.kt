package com.example.androidbaseapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class AndroidBaseApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        initTimber()
    }
    
    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized for debug build")
        }
    }
}