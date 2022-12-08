package dev.danzou.advent.utils

import java.nio.file.Files
import java.io.IOException
import java.lang.Exception
import java.math.BigInteger
import java.nio.file.Path
import java.security.MessageDigest

private const val FILE_PREFIX = "day"

fun readFileLines(name: String): List<String> {
    return try {
        Files.readAllLines(Path.of(name))
    } catch (e: IOException) {
        e.printStackTrace()
        println("occurred while reading $name; returning empty list instead")
        emptyList<String>()
    }
}

fun readOutputLines(): List<String?> {
    val fileName = "outputs/$FILE_PREFIX$executingDayNumber.out"
    val lines = readFileLines(fileName)
    return if (lines.size != 2) {
        println("Expected 2 lines in file " + fileName + " but there were actually " + lines.size)
        listOf(null, null)
    } else lines
}

fun readInputLines(): List<String> {
    return readFileLines(
        "inputs/$FILE_PREFIX$executingDayNumber.in"
    )
}

fun readInputString(): String {
    return java.lang.String.join("\n", readInputLines())
}

/**
 * Returns day number that is currently executing. A pretty big hack.
 * @throws java.util.NoSuchElementException if no stack trace file name starts with "Day" or
 * "day"
 * @return executing day number
 */
val executingDayNumber: Int
    get() = try {
        throw Exception()
    } catch (e: Exception) {
        e.stackTrace.first {
            it.fileName?.startsWith("day", ignoreCase = true) ?: throw Exception(
                "`StackElement!` had no filename"
            )
        }.fileName!!.let {
            it.substring(3, it.indexOf('.'))
        }.toInt()
    }

/**
 * Converts string to md5 hash.
 */
fun String.md5(): String = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray())).toString(16)

operator fun Pair<Int, Int>.plus(other: Pair<Int, Int>): Pair<Int, Int> = Pair(this.first + other.first, this.second + other.second)

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