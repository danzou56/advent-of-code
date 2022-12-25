package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.*

const val DIMENSION = 2

interface Polygon {
    val height: Int
    val width: Int
    val pos: Pos
    val size
        get() = Pair(width, height)
    fun lower(index: Int) = pos[index]
    fun upper(index: Int) = pos[index] + size[index] - 1

    operator fun contains(p: Pos): Boolean
    operator fun contains(other: Polygon): Boolean
    fun isDisjointFrom(other: Polygon): Boolean
    fun intersects(other: Polygon): Boolean =
        !isDisjointFrom(other)

    fun union(other: Polygon): Polygon

    operator fun plus(p: Pos): Polygon
    operator fun minus(p: Pos): Polygon =
        plus(p * -1)
}

object EmptyPolygon : Polygon {
    override val height = 0
    override val width = 0
    override val pos
        get() = throw EmptyPolygonException()

    override fun contains(p: Pos): Boolean = false

    override fun contains(other: Polygon): Boolean = when (other) {
        is EmptyPolygon -> true
        else -> false
    }

    override fun isDisjointFrom(other: Polygon): Boolean = true

    override fun union(other: Polygon): Polygon = other

    override fun plus(p: Pos): Polygon = this

    class EmptyPolygonException : Exception()
}