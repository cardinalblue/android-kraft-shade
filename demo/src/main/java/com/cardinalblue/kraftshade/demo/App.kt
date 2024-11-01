package com.cardinalblue.kraftshade.demo

import android.app.Application
import com.cardinalblue.kraftshade.util.KraftLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initDebugging()
    }

    private fun initDebugging() {
        KraftLogger.debugEnabled = true
        KraftLogger.throwOnError = true
    }
}