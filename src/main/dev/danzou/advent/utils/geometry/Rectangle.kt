package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.Point
import dev.danzou.advent.utils.get
import dev.danzou.advent.utils.toPair
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class Rectangle(bound1: Point, bound2: Point): RectangleUnion(Pair(bound1, bound2)) {
    private val dimension = 2
    private val bound1: Point
        get() = rectangles.first().first
    private val bound2 : Point
        get() = rectangles.first().second

    operator fun contains(other: Rectangle): Boolean {
        return this.contains(other.bound1) && this.contains(other.bound2)
    }

    fun isDisjointFrom(other: Rectangle): Boolean {
        return !this.contains(other.bound1) && !this.contains(other.bound2)
    }

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

    fun lower(dim: Int): Int {
        return min(bound1[dim], bound2[dim])
    }

    fun upper(dim: Int): Int {
        return max(bound1[dim], bound2[dim])
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
