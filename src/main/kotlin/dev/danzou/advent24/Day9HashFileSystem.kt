package dev.danzou.advent24

import dev.danzou.advent.utils.gaussianSum

class Day9HashFileSystem
private constructor(diskMap: Map<Int, Day9FileSystem.RawBlock>) : Day9FileSystem.Day9MapFileSystem() {
  override val diskMap: HashMap<Int, Day9FileSystem.RawBlock> = HashMap(diskMap)
  private val fileMap: MutableMap<Int, Int> =
    diskMap
      .mapNotNull { (address, fileBlock) ->
        when (fileBlock) {
          is Day9FileSystem.RawBlock.File -> fileBlock.id to address
          else -> null
        }
      }
      .toMap()
      .toMutableMap()

  override fun fragment(): Long {
    throw NotImplementedError()
  }

  private tailrec fun nextFree(cur: Int, minSize: Int = 1): Int? {
    val nextEntry = diskMap[cur] ?: return null
    if (nextEntry is Day9FileSystem.RawBlock.Free && nextEntry.size >= minSize) return cur
    else return nextFree(cur + nextEntry.size, minSize)
  }

  private tailrec fun coalesceFrees(start: Int) {
    val freeEntry = diskMap[start]!!
    val nextFreeEntry = diskMap[start + freeEntry.size]
    if (nextFreeEntry == null || nextFreeEntry is Day9FileSystem.RawBlock.File) return
    diskMap[start] = Day9FileSystem.RawBlock.Free(freeEntry.size + nextFreeEntry.size)
    diskMap.remove(start + freeEntry.size)
    coalesceFrees(start)
  }

  override fun compact(): Long {

    tailrec fun step(firstFree: Int?, nextId: Int) {
      if (nextId < 0) return

      assert(nextId in fileMap)
      val last = fileMap[nextId]!!
      val lastEntry = diskMap[last]!!
      firstFree!!
      val nextFree = nextFree(firstFree, lastEntry.size)
      if (nextFree == null || nextFree > last) return step(firstFree, nextId - 1)

      val nextFreeEntry = diskMap[nextFree]!!
      diskMap[nextFree] = lastEntry
      if (nextFreeEntry.size > lastEntry.size) {
        diskMap[nextFree + lastEntry.size] =
          Day9FileSystem.RawBlock.Free(nextFreeEntry.size - lastEntry.size)
      }
      diskMap[last] = Day9FileSystem.RawBlock.Free(lastEntry.size)
      coalesceFrees(last)
      step(nextFree(firstFree), nextId - 1)
    }

    step(nextFree(0, 0)!!, fileMap.keys.max())
    return checksum
  }

  override val checksum: Long
    get() =
      diskMap
        .mapNotNull { (address, fileBlock) ->
          if (fileBlock is Day9FileSystem.RawBlock.File) address.toLong() to fileBlock
          else null
        }
        .sumOf { (address, fileBlock) ->
          ((address + fileBlock.size - 1).gaussianSum() -
              (address - 1).gaussianSum()) * fileBlock.id
        }

  companion object {
    fun fromMap(diskMap: Map<Int, Day9FileSystem.RawBlock>): Day9HashFileSystem {
      return Day9HashFileSystem(diskMap)
    }
  }
}
