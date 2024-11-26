package com.cardinalblue.kraftshade.util

interface SuspendAutoCloseable {
    suspend fun close()
}

suspend inline fun <T : SuspendAutoCloseable, R> T.use(block: (T) -> R): R {
    try {
        return block.invoke(this)
    } finally {
        close()
    }
}