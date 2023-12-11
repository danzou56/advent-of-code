package dev.danzou.advent.utils

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun <R, T> Pair<R, T>.reversed(): Pair<T, R> = Pair(this.second, this.first)

fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply { this[index] = item }

fun <T> Int.times(initial: T, operation: (T) -> T): T = (0 until this).fold(initial) { acc, _ -> operation(acc) }

fun <T> Iterable<T>.frequencyMap(): Map<T, Int> =
    this.groupingBy { it }.eachCount()

inline fun <reified T> String.getValue(): T =
    this.getValues<T>().single()

inline fun <reified T> String.getValues(): List<T> =
    when (T::class) {
        Int::class -> Regex("-?\\d+").findAll(this).map { it.value.toInt() }.toList()
        UInt::class -> Regex("\\d+").findAll(this).map { it.value.toUInt() }.toList()
        Long::class -> Regex("-?\\d+").findAll(this).map { it.value.toLong() }.toList()
        ULong::class -> Regex("\\d+").findAll(this).map { it.value.toULong() }.toList()
        else -> throw UnsupportedOperationException("Type ${T::class.simpleName} not supported for getValues")
    } as List<T>

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
 * Find all k-combinations within the set. Generally not performant; use .pairs() if possible
 */
infix fun <T> Set<T>.choose(k: Int): Set<Set<T>> {
    require(k <= this.size)
    require(k >= 0)

    fun generate(remaining: Set<T>, current: Set<T>, k: Int): Set<Set<T>> {
        if (k == 0) return setOf(current)
        require(remaining.isNotEmpty())
        return remaining.flatMap {
            generate(
                remaining - it,
                current + it,
                k - 1
            )
        }.toSet()
    }

    return generate(this, emptySet(), k)
}

/**
 * Find all k-combinations within the list. Differs in that the resulting elements of the set are
 * lists and so can be easily deconstructed. Generally not performant; use .pairs() if possible.
 */
infix fun <T> List<T>.choose(k: Int): Set<List<T>> {
    return (this.indices.toSet() choose k).map { indices ->
        this.slice(indices)
    }.toSet()
}

/**
 * Find all pairs within the collection. While the return type is a list, this is primarily for
 * easy deconstruction and manipulation, and to improve runtime performance. Uniqueness of elements
 * is guaranteed including when the order of the lists are reversed.
 */
fun <T> Collection<T>.pairs(): List<List<T>> =
    this.flatMap { first ->
        this.mapNotNull { second ->
            setOf(first, second).takeIf { it.size > 1 }
        }
    }.toSet().map { it.toList() }

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
