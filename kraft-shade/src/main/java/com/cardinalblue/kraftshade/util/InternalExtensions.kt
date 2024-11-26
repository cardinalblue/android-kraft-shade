package com.cardinalblue.kraftshade.util

internal val Any?.shortHash: String get() {
    val hashCode = hashCode()
    val positiveHashCode = if (hashCode < 0) {
        -hashCode + 31
    } else hashCode
    return positiveHashCode.toString(36)
}