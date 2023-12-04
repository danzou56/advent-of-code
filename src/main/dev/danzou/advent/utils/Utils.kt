package dev.danzou.advent.utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply { this[index] = item }

fun <T> Int.times(initial: T, operation: (T) -> T): T = (0 until this).fold(initial) { acc, _ -> operation(acc) }

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

/**
 * Super jank class that emulates the functionality of the Kotlin data keyword. Generates a basic
 * toString, equals, and hashCode that should be "good enough". Not advisable for performance
 * critical applications due to the generous use of reflection.
 */
abstract class Data {
    override fun toString(): String {
        val className = this::class.simpleName
        val fields = (this::class.memberProperties as Collection<KProperty1<Any, *>>)
            .map { property -> "${property.name}=${property.get(this)}" }
            .joinToString(",")
        return "$className(${fields})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return (this::class.memberProperties as Collection<KProperty1<Any, *>>)
            .all { property ->
                property.get(this) == property.get(other)
            }
    }

    override fun hashCode(): Int =
        (this::class.memberProperties as Collection<KProperty1<Any, *>>)
            .fold(0) { hash, property ->
                31 * hash + property.get(this).hashCode()
            }
}
