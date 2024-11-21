package com.cardinalblue.kraftshade.demo

import android.app.Application
import com.cardinalblue.kraftshade.util.KraftLogger
import com.cardinalblue.kraftshade.util.KraftLogLevel

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initDebugging()
    }

    private fun initDebugging() {
        KraftLogger.logLevel = KraftLogLevel.VERBOSE
        KraftLogger.throwOnError = true
    }
}