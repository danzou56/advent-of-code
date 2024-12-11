package dev.danzou.advent24

import dev.danzou.advent.utils.gaussianSum
import java.util.*

interface Day9FileSystem {
  fun fragment(): Long
  fun compact(): Long

  val checksum: Long

  companion object {
    fun parseToFsMap(input: String): SortedMap<Int, RawBlock> {
      val diskMap = mutableMapOf<Int, RawBlock>()
      input
          .toList()
          .map(Char::digitToInt)
          .windowed(2, 2, partialWindows = true)
          .map { if (it.size == 1) it + 0 else it }
          .foldIndexed(0) { id, end, (fileSize, freeSize) ->
            val entries =
                listOf(RawBlock.File(id, fileSize), RawBlock.Free(freeSize))
                    .filter { it.size > 0 }
                    .fold(end to emptyList<Pair<Int, RawBlock>>()) {
                        (cur, list),
                        fileBlock ->
                      Pair(cur + fileBlock.size, list + (cur to fileBlock))
                    }
                    .second
            diskMap.putAll(entries)
            end + entries.sumOf { it.second.size }
          }
      return diskMap.toSortedMap()
    }
  }

  sealed interface RawBlock {
    val size: Int

    data class File(val id: Int, override val size: Int) : RawBlock

    data class Free(override val size: Int) : RawBlock
  }

  abstract class Day9MapFileSystem : Day9FileSystem {
    protected abstract val diskMap: Map<Int, RawBlock>

    override val checksum: Long
      get() =
        diskMap
          .mapNotNull { (address, fileBlock) ->
            if (fileBlock is RawBlock.File) address.toLong() to fileBlock
            else null
          }
          .sumOf { (address, fileBlock) ->
            ((address + fileBlock.size - 1).gaussianSum() -
                (address - 1).gaussianSum()) * fileBlock.id
          }
  }
}
