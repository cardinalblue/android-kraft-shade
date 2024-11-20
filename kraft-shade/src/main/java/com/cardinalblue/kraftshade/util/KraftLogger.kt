package com.cardinalblue.kraftshade.util

import android.util.Log

enum class KraftLogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    NONE;

    fun isLoggable(level: KraftLogLevel): Boolean {
        if (this == NONE) return false
        return this.ordinal <= level.ordinal
    }
}

@JvmInline
value class KraftLogger(private val tag: String) {
    fun v(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.VERBOSE)) return
        Log.v(tag, message)
    }

    fun d(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.DEBUG)) return
        Log.d(tag, message)
    }

    fun i(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.INFO)) return
        Log.i(tag, message)
    }

    fun w(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.WARNING)) return
        Log.w(tag, message)
    }

    fun e(message: String, e: Throwable? = null) {
        if (!logLevel.isLoggable(KraftLogLevel.ERROR)) return
        if (e != null) {
            Log.e(tag, message, e)
        } else {
            Log.e(tag, message)
        }
    }

    inline fun tryAndLog(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            e("${e.message}", e)

            if (throwOnError) {
                throw e
            }
        }
    }

    companion object {
        var logLevel = KraftLogLevel.NONE
        var throwOnError = false
    }
}
