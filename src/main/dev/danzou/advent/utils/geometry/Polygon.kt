package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.*

const val DIMENSION = 2

abstract class Polygon(open val height: Int, open val width: Int, val pos: Pos) {
//    val height: Int
//    val width: Int
//    val pos: Pos
    val size
        get() = Pair(width, height)
    fun lower(index: Int) = pos[index]
    fun upper(index: Int) = pos[index] + size[index] - 1

    abstract operator fun contains(p: Pos): Boolean
    abstract operator fun contains(other: Polygon): Boolean
    abstract fun isDisjointFrom(other: Polygon): Boolean
    abstract fun intersects(other: Polygon): Boolean

    abstract fun union(other: Polygon): Polygon

    abstract operator fun plus(p: Pos): Polygon
    operator fun minus(p: Pos): Polygon =
        plus(p * -1)
}

class EmptyPolygon : Polygon(0, 0, Pos(0, 0)) {
    override fun contains(p: Pos): Boolean = false

    override fun contains(other: Polygon): Boolean = when (other) {
        is EmptyPolygon -> true
        else -> false
    }

    override fun isDisjointFrom(other: Polygon): Boolean = true

    override fun intersects(other: Polygon): Boolean = false

    override fun union(other: Polygon): Polygon = other

    override fun plus(p: Pos): Polygon = this
}