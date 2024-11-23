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
        Log.v(tag, "$LOG_PREFIX $message")
    }

    fun d(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.DEBUG)) return
        Log.d(tag, "$LOG_PREFIX $message")
    }

    inline fun d(messageProvider: () -> String) {
        if (!logLevel.isLoggable(KraftLogLevel.DEBUG)) return
        d(messageProvider())
    }

    fun i(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.INFO)) return
        Log.i(tag, "$LOG_PREFIX $message")
    }

    fun w(message: String) {
        if (!logLevel.isLoggable(KraftLogLevel.WARNING)) return
        Log.w(tag, "$LOG_PREFIX $message")
    }

    fun e(message: String, e: Throwable? = null) {
        if (!logLevel.isLoggable(KraftLogLevel.ERROR)) return
        if (e != null) {
            Log.e(tag, "$LOG_PREFIX $message", e)
        } else {
            Log.e(tag, "$LOG_PREFIX $message")
        }
    }

    inline fun tryAndLog(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            e("$LOG_PREFIX ${e.message}", e)

            if (throwOnError) {
                throw e
            }
        }
    }

    companion object {
        const val LOG_PREFIX = "[Kraft]"

        var logLevel = KraftLogLevel.NONE
        var throwOnError = false
    }
}
