package dev.danzou.advent.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.test.assertEquals

abstract class AdventTestRunner(protected val year: Int) {
    protected val day: Int
        get() = this.javaClass.simpleName.drop(3).takeWhile(Char::isDigit).toInt()

    protected val timeout: Duration = Duration.ofSeconds(60)
    private val dataRoot = "src/main/resources/dev/danzou"
    private val basePath = "$dataRoot/advent$year"
    protected val baseInputPath = "$basePath/inputs"
    protected val baseOutputPath = "$basePath/outputs"

    private val input: String = readFileString(
        "$baseInputPath/day$day.in"
    )

    private val expected: List<String?> = readFileLines(
        "$baseOutputPath/day$day.out"
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
                val path = Path.of(name)
                if (path.notExists()) {
                    println("No such file $name; creating empty file")
                    path.createFile()
                }
                val lines = Files.readAllLines(path)
                if (lines.isEmpty() || lines[0].isEmpty())
                    throw IOException("Empty file!")
                else lines
            } catch (e: IOException) {
                e.printStackTrace()
                println("occurred while reading $name; returning empty list instead")
                emptyList()
            }
        }

        fun readFileString(name: String): String = readFileLines(name).joinToString("\n")
    }
}