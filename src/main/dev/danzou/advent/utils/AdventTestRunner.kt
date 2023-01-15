package dev.danzou.advent.utils

import org.junit.jupiter.api.Test
import java.io.IOException
import java.lang.Exception
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals

abstract class AdventTestRunner {
    abstract val year: Int
    /**
     * Day number of currently executing class. A pretty big hack.
     * @throws java.util.NoSuchElementException if no stack trace file name starts with "Day" or
     * "day"
     * @return executing day number
     */
    private val day: Int
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

    private val input: String = readFileLines(
        "inputs/day$day.in"
    ).joinToString("\n")
    private val expected: List<String?> = readFileLines("outputs/day$day.out").let { lines ->
        if (lines.size <= 2) lines
        else listOf(lines.first(), lines.drop(1).joinToString("\n"))
    }

    abstract fun part1(
        input: String = this.input,
    ): Any

    abstract fun part2(
        input: String = this.input,
    ): Any

    @Test
    fun testPart1() {
        val part1 = part1()
        println(part1)
        assertEquals(expected.getOrNull(0), part1.toString())
    }

    @Test
    fun testPart2() {
        val part2 = part2()
        println(part2)
        assertEquals(expected.getOrNull(1), part2.toString())
    }

    companion object {
        fun readFileLines(name: String): List<String> {
            return try {
                val lines = Files.readAllLines(Path.of(name))
                if (lines.isEmpty() || lines[0].isEmpty())
                    throw IOException("Empty file!")
                else lines
            } catch (e: IOException) {
                e.printStackTrace()
                println("occurred while reading $name; returning empty list instead")
                emptyList()
            }
        }
    }
}