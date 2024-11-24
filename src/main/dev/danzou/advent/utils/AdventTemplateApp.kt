package dev.danzou.advent.utils

import java.nio.file.StandardOpenOption
import java.time.LocalDate
import java.time.Month
import kotlin.io.path.Path
import kotlin.io.path.writeText

val adventRootPath = "src/main/dev/danzou"

fun main() {
  //    writeTomorrowsAocFile()
  writeAocFile(20, 4)
}

fun writeTomorrowsAocFile() {
  val tomorrow = LocalDate.now().plusDays(1)
  val year = tomorrow.year
  if (tomorrow.month != Month.DECEMBER) throw RuntimeException("It's not December!")
  val day = tomorrow.dayOfMonth

  writeAocFile(year.mod(100), day)
}

fun writeAocFile(year: Int, day: Int) {
  require(year in 15..99)

  val targetFilePath = Path("$adventRootPath/advent$year/kotlin/Day$day.kt")

  val fileText =
      """
          package dev.danzou.advent$year.kotlin
  
          import dev.danzou.advent.utils.*
          import dev.danzou.advent$year.AdventTestRunner$year
          import org.junit.jupiter.api.Assertions.assertEquals
          import org.junit.jupiter.api.Test
      
          internal class Day$day : AdventTestRunner$year("") {
            override fun part1(input: String): Any {
              TODO()
            }
    
            override fun part2(input: String): Any {
              TODO()
            }
            
            @Test
            fun testExample() {
              ""${'"'}
                
              ""${'"'}
                  .trimIndent()
                  .let { input ->
                    assertEquals(null, part1(input))
                    // assertEquals(null, part2(input))
                  }
            }
          }
        """
          .trimIndent()

  targetFilePath.writeText(fileText, options = arrayOf(StandardOpenOption.CREATE_NEW))
}
