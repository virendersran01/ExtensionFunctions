package com.virtualstudios.extensionfunctions.di_hilt

import android.app.Application
import com.virtualstudios.extensionfunctions.exception_handling.ExceptionHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ExtApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(ExceptionHandler(this))
    }
}