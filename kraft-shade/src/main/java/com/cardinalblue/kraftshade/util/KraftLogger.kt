package com.cardinalblue.kraftshade.util

import android.util.Log

@JvmInline
value class KraftLogger(private val tag: String) {
    fun d(message: String) {
        if (!debugEnabled) return
        Log.d(tag, message)
    }

    fun w(message: String) {
        if (!debugEnabled) return
        Log.w(tag, message)
    }

    fun e(message: String, e: Throwable? = null) {
        if (!debugEnabled) return
        Log.e(tag, message)
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
        var debugEnabled = false
        var throwOnError = false
    }
}
