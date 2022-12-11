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
    if (lines.size <= 2) return lines
    return listOf(lines.first(), lines.drop(1).joinToString("\n"))
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

fun <T> List<T>.update(index: Int, item: T): List<T> = toMutableList().apply { this[index] = item }