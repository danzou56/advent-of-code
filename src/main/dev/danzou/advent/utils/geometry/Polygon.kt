package dev.danzou.advent.utils.geometry

import dev.danzou.advent.utils.Pos

interface Polygon {
    operator fun contains(p: Pos): Boolean
    operator fun contains(other: Polygon): Boolean
    fun isDisjointFrom(other: Polygon): Boolean
    fun intersects(other: Polygon): Boolean =
        !isDisjointFrom(other)
    fun intersect(other: Polygon): Polygon
}