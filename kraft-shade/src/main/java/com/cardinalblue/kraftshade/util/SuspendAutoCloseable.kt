package com.cardinalblue.kraftshade.util

interface SuspendAutoCloseable {
    suspend fun close(deleteRecursively: Boolean = true)
}

suspend inline fun <T : SuspendAutoCloseable, R> T.use(deleteRecursively: Boolean = true, block: (T) -> R): R {
    try {
        return block.invoke(this)
    } finally {
        close(deleteRecursively)
    }
}