package dev.danzou.advent.utils

import java.math.BigInteger
import java.security.MessageDigest

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply { this[index] = item }

fun <T> Int.times(initial: T, operation: (T) -> T): T = (0 until this).fold(initial) { acc, _ -> operation(acc) }
