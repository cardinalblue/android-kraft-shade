package com.cardinalblue.kraftshade

object KraftShadeNative {
    init {
        System.loadLibrary("kraft-shade")
    }

    external fun YUVtoRBGA(yuv: ByteArray, width: Int, height: Int, out: IntArray)

    external fun YUVtoARBG(yuv: ByteArray, width: Int, height: Int, out: IntArray)
}
