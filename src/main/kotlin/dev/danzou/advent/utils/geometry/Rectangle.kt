package dev.danzou.advent.utils.geometry

import kotlin.math.abs
import kotlin.math.min

open class Rectangle(bound1: Point, bound2: Point): RectangleUnion(Pair(bound1, bound2)) {
    private val dimension = 2
    private val bound1: Point
        get() = _rectangles.first().first
    private val bound2 : Point
        get() = _rectangles.first().second

    fun distanceTo(q: Point): Int {
        if (this.contains(q)) return 0
        var dist = 0
        for (i in 0 until dimension) {
            if (q[i] < lower(i) || q[i] > upper(i)) {
                val part: Int = min(
                    abs(q[i] - lower(i)),
                    abs(q[i] - upper(i))
                )
                dist += part * part
            }
        }
        return dist
    }

    fun half(cutDim: Int, splitter: Point, side: Boolean): Rectangle {
        val other: Point?
        val template: Point?
        if ((bound1[cutDim] < bound2[cutDim]) xor side) {
            template = bound1
            other = bound2
        } else {
            template = bound2
            other = bound1
        }
        val bounds = (0 until dimension).map { i ->
            if (i == cutDim) {
                splitter[i]
            } else {
                template[i]
            }
        }.toPair()
        return Rectangle(bounds, other)
    }

    fun lowerHalf(cutDim: Int, splitter: Point): Rectangle {
        return half(cutDim, splitter, true)
    }

    fun upperHalf(cutDim: Int, splitter: Point): Rectangle {
        return half(cutDim, splitter, false)
    }

    val longestDimension: Int
        get() {
            var cutDim = 0
            var max = 0
            for (i in 0 until dimension) {
                if (upper(i) - lower(i) > max) {
                    cutDim = i
                    max = upper(i) - lower(i)
                }
            }
            return cutDim
        }
}
