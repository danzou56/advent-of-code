package dev.danzou.advent.utils

import java.lang.IndexOutOfBoundsException

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(this.first + other.first, this.second + other.second)

operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(this.first - other.first, this.second - other.second)

operator fun Pair<Int, Int>.rangeTo(other: Pair<Int, Int>): Iterable<Pair<Int, Int>> {
    val diff = other - this
    if (diff.first == 0 && diff.second == 0) {
        return listOf(this)
    }
    if (diff.first != 0 && diff.second != 0) {
        throw NotImplementedError("Only strictly horizontal or strictly vertical directions are supported")
    }

    return when {
        diff.first > 0 -> (this.first..other.first).map { Pair(it, this.second) }
        diff.first < 0 -> (this.first downTo other.first).map { Pair(it, this.second) }
        diff.second > 0 -> (this.second..other.second).map { Pair(this.first, it) }
        // if prior three are false, diff.second < 0 is necessarily true
        else -> (this.second downTo other.second).map { Pair(this.first, it) }
    }
}

infix fun Pair<Int, Int>.until(other: Pair<Int, Int>): Iterable<Pair<Int, Int>> {
    val diff = other - this
    if (diff.first == 0 && diff.second == 0) {
        return emptyList()
    }
    if (diff.first != 0 && diff.second != 0) {
        throw NotImplementedError("Only strictly horizontal or strictly vertical directions are supported")
    }

    return when {
        diff.first > 0 -> (this.first until other.first).map { Pair(it, this.second) }
        diff.first < 0 -> (this.first downTo other.first + 1).map { Pair(it, this.second) }
        diff.second > 0 -> (this.second until other.second).map { Pair(this.first, it) }
        // if prior three are false, diff.second < 0 is necessarily true
        else -> (this.second downTo other.second + 1).map { Pair(this.first, it) }
    }
}

fun <T> List<T>.toPair(): Pair<T, T> {
    if (this.size != 2) {
        throw IllegalArgumentException("List is not of length 2!")
    }
    return Pair(this[0], this[1])
}

fun <T> List<T>.toTriple(): Triple<T, T, T> {
    if (this.size != 3) {
        throw IllegalArgumentException("List is not of length 3!")
    }
    return Triple(this[0], this[1], this[2])
}

operator fun <T> Pair<T, T>.get(i: Int): T = when (i) {
    0 -> this.first
    1 -> this.second
    else -> throw IndexOutOfBoundsException()
}