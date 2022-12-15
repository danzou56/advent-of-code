package dev.danzou.advent.utils

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Rectangle(val bound1: Point, val bound2: Point) {
    private val dimension = 2

    operator fun contains(q: Point): Boolean {
        for (i in 0 until dimension) {
            val coord: Int = q[i]
            if (coord < lower(i) || coord > upper(i)) return false
        }
        return true
    }

    operator fun contains(c: Rectangle): Boolean {
        return this.contains(c.bound1) && this.contains(c.bound2)
    }

    fun isDisjointFrom(c: Rectangle): Boolean {
        return !this.contains(c.bound1) && !this.contains(c.bound2)
    }

    fun intersects(c: Rectangle): Boolean {
        return !isDisjointFrom(c)
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

    fun lowerHalf(cutDim: Int, splitter: Point): Rectangle? {
        return half(cutDim, splitter, true)
    }

    fun upperHalf(cutDim: Int, splitter: Point): Rectangle? {
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
