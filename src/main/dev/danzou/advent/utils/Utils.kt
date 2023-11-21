package dev.danzou.advent.utils

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply { this[index] = item }

fun <T> Int.times(initial: T, operation: (T) -> T): T = (0 until this).fold(initial) { acc, _ -> operation(acc) }

fun Int.pow(i: Int): Int =
    i.times(1) { it * this }

fun Int.gaussianSum() =
    this * (this + 1) / 2

fun IntRange.intersects(that: IntRange): Boolean =
    this.first >= that.first || this.last <= that.last

fun IntRange.isDisjoint(that: IntRange): Boolean =
    this.first > that.last || this.last < that.first

fun IntRange.isDisjointOrBorders(that: IntRange): Boolean =
    this.first >= that.last || this.last <= that.first

operator fun IntRange.contains(that: IntRange): Boolean =
    that.first in this && that.last in this

fun <T> permutationsOf(sets: List<Set<T>>): Set<List<T>> {
    val res = mutableSetOf<List<T>>()
    fun generate(cur: List<T>): Any {
        val depth = cur.size
        if (depth == sets.size) return res.add(cur)
        return sets[depth].forEach { generate(cur + it) }
    }
    generate(emptyList())

    return res
}