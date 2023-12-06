package dev.danzou.advent.utils

import kotlin.math.max
import kotlin.math.min

infix fun IntRange.intersect(that: IntRange): IntRange =
    max(this.first, that.first)..min(this.last, that.last)

fun IntRange.intersects(that: IntRange): Boolean =
    this.first <= that.last && this.last >= that.first

infix fun LongRange.intersect(that: LongRange): LongRange =
    max(this.first, that.first)..min(this.last, that.last)

fun LongRange.intersects(that: LongRange): Boolean =
    this.first <= that.last && this.last >= that.first

fun IntRange.isDisjoint(that: IntRange): Boolean =
    this.first > that.last || this.last < that.first

fun IntRange.isDisjointOrBorders(that: IntRange): Boolean =
    this.first >= that.last || this.last <= that.first

operator fun IntRange.contains(that: IntRange): Boolean =
    that.first in this && that.last in this