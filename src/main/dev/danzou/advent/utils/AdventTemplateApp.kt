package dev.danzou.advent.utils

import java.nio.file.StandardOpenOption
import java.time.LocalDate
import java.time.Month
import kotlin.io.path.Path
import kotlin.io.path.writeText

fun main() {
    val tomorrow = LocalDate.now().plusDays(1)
    val year = tomorrow.year.toString().takeLast(2)
    if (tomorrow.month != Month.DECEMBER) throw RuntimeException("It's not December!")
    val day = tomorrow.dayOfMonth

    val adventRootPath = "src/main/dev/danzou"
    val targetFilePath = Path("$adventRootPath/advent$year/kotlin/Day$day.kt")

    val fileText = """
        package dev.danzou.advent$year.kotlin

        import dev.danzou.advent$year.AdventTestRunner$year
    
        internal class Day$day : AdventTestRunner$year() {
            override fun part1(input: String): Any {
                TODO("Not yet implemented")
            }
    
            override fun part2(input: String): Any {
                TODO("Not yet implemented")
            }
        }
    """.trimIndent()
    targetFilePath.writeText(fileText, options = arrayOf(StandardOpenOption.CREATE_NEW))
}