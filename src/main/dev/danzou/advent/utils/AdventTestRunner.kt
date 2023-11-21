package dev.danzou.advent.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.createFile
import kotlin.io.path.notExists
import kotlin.io.path.writeText
import kotlin.test.assertEquals

abstract class AdventTestRunner(protected val year: Int, protected val name: String? = null) {
    protected val day: Int
        get() = this.javaClass.simpleName.drop(3).takeWhile(Char::isDigit).toInt()

    protected open val timeout: Duration = Duration.ofSeconds(60)
    private val dataRoot = "data/advent"
    private val basePath = "$dataRoot/advent$year"
    protected val baseInputPath = "$basePath/inputs"
    protected val baseOutputPath = "$basePath/outputs"

    private val input: String = readFileString(
        "$baseInputPath/day$day.in",
        "$AOC_BASE_URL/20$year/day/$day/input"
    ).also { require(it.isNotEmpty()) }

    private val expected: List<String?> = readFileLines(
        "$baseOutputPath/day$day.out"
    ).let { lines ->
        if (lines.size <= 2) lines
        else listOf(lines.first(), lines.drop(1).joinToString("\n"))
    }

    abstract fun part1(input: String = this.input): Any

    abstract fun part2(input: String = this.input): Any

    @Test
    fun testPart1() {
        val part1 = assertTimeoutPreemptively(timeout, ::part1)
        println(part1)
        assertEqualAnswer(expected.getOrNull(0), part1)
    }

    @Test
    fun testPart2() {
        val part2 = assertTimeoutPreemptively(timeout, ::part2)
        println(part2)
        assertEqualAnswer(expected.getOrNull(1), part2)
    }

    companion object {
        const val AOC_TOKEN_KEY = "AOC_TOKEN"
        const val AOC_BASE_URL = "https://adventofcode.com"

        fun readFileLines(name: String, fallbackUrl: String? = null): List<String> {
            return try {
                val path = Path.of(name)
                if (path.notExists()) {
                    println("No such file $name; creating empty file")
                    path.createFile()
                }

                val lines = Files.readAllLines(path)
                if (lines.isNotEmpty() && lines[0].isNotEmpty()) return lines
                if (fallbackUrl == null) throw IOException("Empty file")
                return DotEnv[AOC_TOKEN_KEY]?.let { token ->
                    val httpRequest = HttpRequest.newBuilder()
                        .uri(URI(fallbackUrl))
                        .headers("Cookie", "session=$token")
                        .GET()
                        .build()
                    val client = HttpClient.newHttpClient()
                    val response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString())
                    if (response.statusCode() == 200) {
                        path.writeText(response.body())
                        println("Retrieved AOC input and wrote into input file")
                        return@let Files.readAllLines(path)
                    }
                    throw IOException("AOC fallback URL $fallbackUrl responded with code ${response.statusCode()}")
                } ?: throw IllegalArgumentException("Fallback URL provided but no token")
            } catch (e: IOException) {
                e.printStackTrace()
                println("occurred while reading $name; returning empty list instead")
                emptyList()
            }
        }

        fun readFileString(name: String, fallbackUrl: String? = null): String =
            readFileLines(name, fallbackUrl).joinToString("\n")

        fun assertEqualAnswer(expected: String?, actual: Any?) {
            when (actual) {
                is AsciiArt -> if (actual.isText) assertEquals(expected, actual.text)
                    else assertEquals(expected, actual.art)
                else -> assertEquals(expected, actual.toString())
            }
        }
    }
}