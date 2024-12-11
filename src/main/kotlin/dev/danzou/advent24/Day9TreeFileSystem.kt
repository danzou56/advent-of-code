package dev.danzou.advent24

import java.util.*
import kotlin.math.min

class Day9TreeFileSystem
private constructor(diskMap: Map<Int, Day9FileSystem.RawBlock>) :
    Day9FileSystem.Day9MapFileSystem() {
  override val diskMap: TreeMap<Int, Day9FileSystem.RawBlock> = TreeMap(diskMap)
  private val size =
      diskMap.values.filterIsInstance<Day9FileSystem.RawBlock.File>().sumOf { it.size }

  private tailrec fun nextFree(cur: Int): Int {
    return if (cur !in diskMap || diskMap[cur] is Day9FileSystem.RawBlock.Free) cur
    else nextFree(cur + diskMap[cur]!!.size)
  }

  override fun fragment(): Long {
    tailrec fun step(firstFree: Int) {
      val last = diskMap.keys.last()
      if (diskMap.getValue(last) is Day9FileSystem.RawBlock.Free) {
        diskMap.remove(last)
        return step(firstFree)
      }

      val lastBlock = diskMap.getValue(last) as Day9FileSystem.RawBlock.File
      if (last + lastBlock.size == this.size) return

      val firstFreeBlock = diskMap.getValue(firstFree)

      val newBlock =
          Day9FileSystem.RawBlock.File(
              lastBlock.id,
              min(firstFreeBlock.size, lastBlock.size),
          )
      diskMap[firstFree] = newBlock
      diskMap.remove(last)!!

      if (lastBlock.size > firstFreeBlock.size) {
        diskMap[last] =
            Day9FileSystem.RawBlock.File(
                lastBlock.id,
                lastBlock.size - firstFreeBlock.size,
            )
      }
      if (lastBlock.size < firstFreeBlock.size) {
        diskMap[firstFree + newBlock.size] =
            Day9FileSystem.RawBlock.Free(
                firstFreeBlock.size - lastBlock.size,
            )
      }

      return step(nextFree(firstFree + newBlock.size))
    }

    step(nextFree(0))
    return checksum
  }

  override fun compact(): Long {
    throw NotImplementedError()
  }

  companion object {
    fun fromMap(map: Map<Int, Day9FileSystem.RawBlock>): Day9TreeFileSystem =
        Day9TreeFileSystem(map)
  }
}
