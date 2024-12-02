package dev.danzou.advent.utils.geometry3

import java.lang.IndexOutOfBoundsException

typealias Pos3 = Triple<Int, Int, Int>
typealias Pos3L = Triple<Long, Long, Long>

operator fun Triple<Int, Int, Int>.plus(other: Triple<Int, Int, Int>): Triple<Int, Int, Int> =
    Triple(this.first + other.first, this.second + other.second, this.third + other.third)

@JvmName("plusLong")
operator fun Triple<Long, Long, Long>.plus(other: Triple<Long, Long, Long>): Triple<Long, Long, Long> =
    Triple(this.first + other.first, this.second + other.second, this.third + other.third)

operator fun <T> Triple<T, T, T>.get(i: Int): T = when (i) {
    0 -> this.first
    1 -> this.second
    2 -> this.third
    else -> throw IndexOutOfBoundsException()
}

fun <T> List<T>.toTriple(): Triple<T, T, T> {
    if (this.size != 3) {
        throw IllegalArgumentException("List is not of length 3!")
    }
    return Triple(this[0], this[1], this[2])
}

val <T, U, V> Triple<T, U, V>.x: T
    get() = this.first

val <T, U, V> Triple<T, U, V>.y: U
    get() = this.second

val <T, U, V> Triple<T, U, V>.z: V
    get() = this.third