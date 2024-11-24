package com.cardinalblue.kraftshade.util

import android.util.Log

@JvmInline
value class KraftLogger(private val tag: String) {
    fun v(message: String) {
        if (!logLevel.isLoggable(Level.VERBOSE)) return
        Log.v(tag, "$LOG_PREFIX $message")
    }

    fun d(message: String) {
        if (!logLevel.isLoggable(Level.DEBUG)) return
        Log.d(tag, "$LOG_PREFIX $message")
    }

    inline fun d(messageProvider: () -> String) {
        if (!logLevel.isLoggable(Level.DEBUG)) return
        d(messageProvider())
    }

    fun i(message: String) {
        if (!logLevel.isLoggable(Level.INFO)) return
        Log.i(tag, "$LOG_PREFIX $message")
    }

    fun w(message: String) {
        if (!logLevel.isLoggable(Level.WARNING)) return
        Log.w(tag, "$LOG_PREFIX $message")
    }

    fun e(message: String, e: Throwable? = null) {
        if (!logLevel.isLoggable(Level.ERROR)) return
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

        var logLevel = Level.NONE
        var throwOnError = false
    }

    enum class Level {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        NONE;

        fun isLoggable(level: Level): Boolean {
            if (this == NONE) return false
            return this.ordinal <= level.ordinal
        }
    }
}
