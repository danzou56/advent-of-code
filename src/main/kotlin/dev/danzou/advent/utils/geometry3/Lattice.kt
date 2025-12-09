package dev.danzou.advent.utils.geometry3

import dev.danzou.advent.utils.pow
import kotlin.math.absoluteValue

fun Pos3.manhattanDistanceTo(that: Pos3): Int =
    (this.x - that.x).absoluteValue + (this.y - that.y).absoluteValue + (this.z - that.z).absoluteValue

fun Pos3.squaredDistanceTo(that: Pos3): Int =
    (this.x - that.x).pow(2) + (this.y - that.y).pow(2) + (this.z - that.z).pow(2)

fun Pos3L.squaredDistanceTo(that: Pos3L): Long =
  (this.x - that.x).pow(2) + (this.y - that.y).pow(2) + (this.z - that.z).pow(2)