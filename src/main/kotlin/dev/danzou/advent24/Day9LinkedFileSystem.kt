package dev.danzou.advent24

import kotlin.math.min

class Day9LinkedFileSystem
private constructor(private var head: Block, private var tail: Block) : Day9FileSystem {
  init {
    // Makes logic easier since our inputs are always more than two blocks long
    require(head !== tail) { "File system must have at least two blocks" }
  }

  private val size: Int

  init {
    var size = 0
    var cur: Block? = head
    while (cur != null) {
      if (cur is Block.File) {
        size += cur.size
      }
      cur = cur.next
    }
    this.size = size
  }

  private tailrec fun nextFree(from: Block = head, min: Int = 1): Block.Free? {
    if (from is Block.Free && from.size >= min) return from
    return nextFree(from.next ?: return null, min)
  }

  private tailrec fun previousFile(from: Block, id: Int? = null): Block.File? {
    if (from is Block.File && (id == null || from.id == id)) return from
    return previousFile(from.prev ?: return null, id)
  }

  private tailrec fun coalesceFrees(free: Block.Free) {
    val nextFree = free.next
    if (nextFree !is Block.Free) return
    val newFree = Block.Free(
      free.address,
      free.size + nextFree.size
    )
    replace(free, newFree)
    remove(nextFree)
    coalesceFrees(newFree)
  }

  private fun replace(old: Block, new: Block) {
    old.prev?.next = new
    old.next?.prev = new

    new.prev = old.prev
    new.next = old.next

    if (old === head) head = new
    if (old === tail) tail = new
  }

  private fun remove(block: Block) {
    val prev = block.prev
    if (prev != null) {
      prev.next = block.next
      block.next?.prev = prev
    } else {
      head = block.next!!
    }
    val next = block.next
    if (next != null) {
      next.prev = block.prev
      block.prev?.next = next
    } else {
      tail = block.prev!!
    }
  }

  private fun insert(new: Block, after: Block) {
    after.next?.prev = new
    new.next = after.next

    after.next = new
    new.prev = after

    if (after === tail) tail = new
  }

  override fun fragment(): Long {
    tailrec fun step(firstFree: Block.Free?) {
      if (firstFree == null) return
      if (tail.address + tail.size == this.size) return
      if (tail is Block.Free) {
        remove(tail)
        return step(firstFree)
      }

      val targetBlock = tail as Block.File
      val newBlock =
          Block.File(
              targetBlock.id,
              firstFree.address,
              min(targetBlock.size, firstFree.size),
          )

      replace(firstFree, newBlock)
      remove(tail)
      if (firstFree.size > targetBlock.size)
          insert(
              Block.Free(
                  newBlock.address + newBlock.size,
                  firstFree.size - newBlock.size,
              ),
              newBlock)
      if (firstFree.size < targetBlock.size)
          insert(
              Block.File(
                  newBlock.id,
                  tail.address,
                  targetBlock.size - newBlock.size,
              ),
              tail)

//      assert(checksum > 0)
      step(nextFree(newBlock))
    }
    step(nextFree(head))
    return checksum
  }

  override fun compact(): Long {
    tailrec fun step(firstFree: Block?, lastFile: Block.File?) {
//      println(mapFs())
      if (lastFile == null) return

      val nextFree = nextFree(firstFree!!, lastFile.size)
      if (nextFree == null || nextFree.address > lastFile.address)
          return step(firstFree, previousFile(lastFile, lastFile.id - 1))

      val newBlock =
          Block.File(
              lastFile.id,
              nextFree.address,
              lastFile.size,
          )

      replace(nextFree, newBlock)
      run {
        val newFree = Block.Free(lastFile.address, lastFile.size)
        replace(lastFile, newFree)
        if (newFree.prev is Block.Free) coalesceFrees(newFree.prev as Block.Free)
        else coalesceFrees(newFree)
      }
      if (nextFree.size > newBlock.size)
          insert(
              Block.Free(newBlock.address + newBlock.size, nextFree.size - newBlock.size),
              newBlock)

      return step(
          if (firstFree.address == nextFree.address) nextFree(newBlock)
          else firstFree,
          previousFile(lastFile, lastFile.id - 1))
    }

    step(nextFree(head), previousFile(tail))
    return checksum
  }

  fun mapFs(): String {
    val out = StringBuilder()
    var cur: Block? = head
    while (cur != null) {
      out.append(
          when (cur) {
                is Block.File -> cur.id.digitToChar()
                is Block.Free -> '.'
              }
              .toString()
              .repeat(cur.size))
      cur = cur.next
    }
    return out.toString()
  }

  override val checksum: Long
    get() {
      var checksum = 0L
      var cur: Block? = head
      while (cur != null) {
        checksum +=
            when (cur) {
              is Block.File ->
                  (cur.address..<(cur.address + cur.size)).sumOf {
                    it.toLong() * (cur as Block.File).id.toLong()
                  }
              is Block.Free -> 0L
            }
//        if (cur.next != null && cur.address + cur.size != cur.next!!.address) {
//          assert(cur.address + cur.size == cur.next!!.address)
//        }
        cur = cur.next
      }
      return checksum
    }

  companion object {
    fun fromMap(diskMap: Map<Int, Day9FileSystem.RawBlock>): Day9LinkedFileSystem {
      require(diskMap.isNotEmpty()) { "At least one file block is required" }

      val diskMapEntries = diskMap.entries.sortedBy { it.key }
      require(diskMapEntries.first().value is Day9FileSystem.RawBlock.File) {
        "First block must be a file"
      }

      val head = run {
        val firstEntry = diskMapEntries.first()
        val firstBlock = firstEntry.value as Day9FileSystem.RawBlock.File
        Block.File(firstBlock.id, 0, firstBlock.size)
      }
      var curBlock: Block = head
      diskMapEntries.drop(1).forEach { (address, rawBlock) ->
        assert(curBlock.address + curBlock.size == address)
        curBlock.next =
            when (rawBlock) {
              is Day9FileSystem.RawBlock.File ->
                  Block.File(rawBlock.id, address, rawBlock.size, prev = curBlock)
              is Day9FileSystem.RawBlock.Free ->
                  Block.Free(address, rawBlock.size, prev = curBlock)
            }
        curBlock = curBlock.next!!
      }
      val tail = curBlock
      return Day9LinkedFileSystem(head, tail)
    }
  }

  sealed interface Block {
    val address: Int
    val size: Int
    var prev: Block?
    var next: Block?

    data class File(
        val id: Int,
        override val address: Int,
        override val size: Int,
        override var prev: Block? = null,
        override var next: Block? = null
    ) : Block {
      override fun toString(): String {
        return "Block.File(" +
            "id=${id}," +
            "address=${address}," +
            "size=${size}," +
            "prev=${prev?.address}," +
            "next=${next?.address})"
      }
    }

    data class Free(
        override val address: Int,
        override val size: Int,
        override var prev: Block? = null,
        override var next: Block? = null
    ) : Block {
      override fun toString(): String {
        return "Block.Free(" +
            "address=${address}," +
            "size=${size}," +
            "prev=${prev?.address}," +
            "next=${next?.address})"
      }
    }
  }
}
