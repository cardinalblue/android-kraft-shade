package com.cardinalblue.kraftshade.model

import android.opengl.Matrix

open class GlMat(
    val arr: FloatArray,
) {
    val dimension = when(arr.size) {
        4 -> 2
        9 -> 3
        16 -> 4
        else -> throw IllegalArgumentException("Invalid float array size: ${arr.size}")
    }

    constructor(dimension: Int) : this(FloatArray(dimension * dimension))

    fun setIdentity() {
        for (i in 0 until dimension) {
            for (j in 0 until dimension) {
                arr[i * dimension + j] = if (i == j) 1f else 0f
            }
        }
    }
}

class GlMat2 : GlMat{
    constructor() : super(2)
    constructor(arr: FloatArray) : super(arr) {
        require(arr.size == 4) { "size has to be 4" }
    }
}

class GlMat3 : GlMat {
    constructor() : super(3)
    constructor(arr: FloatArray) : super(arr) {
        require(arr.size == 9) { "siz has to be 9" }
    }
}


class GlMat4 : GlMat{
    constructor() : super(4)
    constructor(arr: FloatArray) : super(arr) {
        require(arr.size == 16) { "invalid float array size: ${arr.size}" }
    }

    fun translate2D(x: Float, y: Float) {
        Matrix.translateM(arr, 0, x, y, 0f)
    }

    fun scale2D(x: Float, y: Float) {
        Matrix.scaleM(arr, 0, x, y, 1f)
    }

    fun scale2D(x: Float, y: Float, pivotX: Float, pivotY: Float) {
        translate2D(pivotX, pivotY)
        scale2D(x, y)
        translate2D(-pivotX, -pivotY)
    }

    fun rotate2D(rotation: Float) {
        Matrix.rotateM(arr, 0, rotation, 0f, 0f, 1f)
    }
}

fun FloatArray.asGlMat(): GlMat {
    return when (size) {
        4 -> GlMat2()
        9 -> GlMat3()
        16 -> GlMat4()
        else -> throw IllegalArgumentException("Invalid float array size: $size")
    }.also { mat -> copyInto(mat.arr) }
}
