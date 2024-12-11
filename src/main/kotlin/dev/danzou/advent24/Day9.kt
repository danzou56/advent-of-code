package dev.danzou.advent24

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day9 : AdventTestRunner24("Disk Fragmenter") {

  override fun part1(input: String): Long {
    val rawBlockMap = Day9FileSystem.parseToFsMap(input)
    val fs = Day9LinkedFileSystem.fromMap(rawBlockMap)
    return fs.fragment()
  }

  override fun part2(input: String): Long {
    val rawBlockMap = Day9FileSystem.parseToFsMap(input)
    val fs = Day9LinkedFileSystem.fromMap(rawBlockMap)
    return fs.compact()
  }

  @Test
  fun testExample() {
    """
      2333133121414131402
    """
        .trimIndent()
        .let { input ->
          assertEquals(1928, part1(input))
          assertEquals(2858, part2(input))
        }
  }
}
