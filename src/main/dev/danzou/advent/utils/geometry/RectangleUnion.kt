package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.Point
import dev.danzou.advent.utils.Pos
import dev.danzou.advent.utils.get
import kotlin.math.max
import kotlin.math.min

private typealias Rect = Pair<Point, Point>

open class RectangleUnion(internal val rectangles: List<Rect>) : Polygon {
    private val dimension = 2

    constructor(rectangle: Rect) : this(listOf(rectangle))

    override fun contains(p: Pos): Boolean =
        !rectangles.map { rect -> (0 until dimension).map { dim ->
            p[dim] < rect.lower(dim) || p[dim] > rect.upper(dim)
        }.reduce(Boolean::or) }.reduce(Boolean::or)

    override fun contains(other: Polygon): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDisjointFrom(other: Polygon): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersects(other: Polygon): Boolean {
        TODO("Not yet implemented")
    }

    override fun intersect(other: Polygon): Polygon {
        TODO("Not yet implemented")
    }

    fun intersect(other: RectangleUnion): RectangleUnion =
        RectangleUnion(rectangles + other.rectangles)
}

fun Rect.lower(dim: Int): Int {
    return min(this.first[dim], this.second[dim])
}

fun Rect.upper(dim: Int): Int {
    return max(this.first[dim], this.second[dim])
}