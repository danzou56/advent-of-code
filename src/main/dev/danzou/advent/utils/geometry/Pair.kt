package dev.danzou.advent.utils.geometry

import java.lang.IndexOutOfBoundsException

// Absolutely bizarre way, but idk what I'm doing and it seems aight ig
inline operator fun <reified T : Number> Pair<T, T>.plus(other: Pair<T, T>): Pair<T, T> =
    when (T::class) {
        Int::class -> Pair(this.first as Int + other.first as Int, this.second as Int + other.second as Int)
        Long::class -> Pair(this.first as Long + other.first as Long, this.second as Long + other.second as Long)
        else -> throw IllegalArgumentException()
    } as Pair<T, T>

operator fun Pair<Int, Int>.minus(other: Pair<Int, Int>): Pair<Int, Int> =
    Pair(this.first - other.first, this.second - other.second)

operator fun Pair<Int, Int>.times(i: Int): Pair<Int, Int> =
    Pair(this.first * i, this.second * i)

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

operator fun <T> Pair<T, T>.get(i: Int): T = when (i) {
    0 -> this.first
    1 -> this.second
    else -> throw IndexOutOfBoundsException()
}
