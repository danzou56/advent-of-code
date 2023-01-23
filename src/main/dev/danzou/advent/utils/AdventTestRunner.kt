package dev.danzou.advent.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.test.assertEquals

abstract class AdventTestRunner(private val year: Int) {
    private val day: Int
        get() = this.javaClass.simpleName.drop(3).toInt()

    protected val timeout: Duration = Duration.ofSeconds(60)
    private val DATA_ROOT = "src/main/resources/dev/danzou"
    private val basePath = "$DATA_ROOT/advent$year"

    private val input: String = readFileLines(
        "$basePath/inputs/day$day.in"
    ).joinToString("\n")

    private val expected: List<String?> = readFileLines(
        "$basePath/outputs/day$day.out"
    ).let { lines ->
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
        val part1 = assertTimeoutPreemptively(timeout, ::part1)
        println(part1)
        assertEquals(expected.getOrNull(0), part1.toString())
    }

    @Test
    fun testPart2() {
        val part2 = assertTimeoutPreemptively(timeout, ::part2)
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