package com.cardinalblue.kraftshade.util

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.TimedValue

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

    inline fun <T> measureAndLog(
        taskName: String,
        configuration: String? = null,
        block: () -> T
    ) : T {
        val startNano = System.nanoTime()
        val result = block()
        val endNano = System.nanoTime()
        val time = (endNano - startNano).nanoseconds
        if (configuration == null) {
            d("[$taskName] took $time")
        } else {
            d("[$taskName] with [$configuration] took $time")
        }
        return result
    }

    inline fun tryAndLog(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            if (e is CancellationException) {
                d("a job was cancelled (probably due to slow)")
                return
            }

            e("${e.message}", e)

            if (throwOnError) {
                throw e
            }
        }
    }

    companion object {
        const val LOG_PREFIX = "[Kraft]"

        var logLevel = Level.NONE
        var throwOnError = false

        /**
         * This is useful for logging debug information that is expensive to compute.
         */
        fun stringOrEmptyBasedOnLevel(
            level: Level,
            stringBuilder: () -> String
        ): String {
            return if (logLevel.isLoggable(level)) {
                stringBuilder()
            } else ""
        }

        /**
         * This is useful for logging debug information that is expensive to compute, so if the
         * logLevel is higher than DEBUG, we can avoid the computation.
         */
        fun debugStringOrEmpty(stringBuilder: () -> String): String {
            return stringOrEmptyBasedOnLevel(Level.DEBUG, stringBuilder)
        }
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
