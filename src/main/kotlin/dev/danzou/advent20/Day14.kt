package dev.danzou.advent20

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day14 : AdventTestRunner20("Docking Data") {

  fun createSetFunction(setMaskString: String): (Long) -> Long =
      createSetMasks(setMaskString).let { (orMask, andMask) ->
        { l: Long -> l.or(orMask).and(andMask) }
      }

  fun createSetMasks(setMaskString: String): Pair<Long, Long> =
      setMaskString
          .removePrefix("mask = ")
          .map { c ->
            when (c) {
              // or mask; and mask
              'X' -> '0' to '1'
              else -> c to c
            }
          }
          .unzip()
          .let { (orMask, andMask) ->
            Pair(
                orMask.joinToString("").toLong(2),
                andMask.joinToString("").toLong(2),
            )
          }

  fun extractAddrImm(storeInstr: String): Pair<Long, Long> =
      Regex("mem\\[(\\d+)] = (\\d+)").matchEntire(storeInstr)!!.destructured.let {
          (addr, imm) ->
        addr.toLong() to imm.toLong()
      }

  override fun part1(input: String): Long {
    return input
        .lines()
        .fold<String, Pair<Map<Long, Long>, ((Long) -> Long)?>>(Pair(emptyMap(), null)) {
            (memory, set),
            instr ->
          when {
            instr.startsWith("mask = ") -> memory to createSetFunction(instr)
            else ->
                extractAddrImm(instr).let { (addr, imm) ->
                  Pair(memory + mapOf(addr to set!!(imm)), set)
                }
          }
        }
        .first
        .values
        .sum()
  }

  override fun part2(input: String): Long {
    fun makeMasks(maskStr: String): (Long) -> List<Long> {
      val baseOrMask =
          maskStr
              .removePrefix("mask = ")
              .map {
                when (it) {
                  '1' -> '1'
                  else -> '0'
                }
              }
              .joinToString("")
              .toLong(2)
      val setMaskStrs =
          maskStr.removePrefix("mask = ").fold(listOf("")) { maskStrs, c ->
            when (c) {
              'X' -> maskStrs.flatMap { listOf(it + '0', it + '1') }
              else -> maskStrs.map { it + 'X' }
            }
          }
      val setMasks =
          setMaskStrs.map(::createSetMasks).map { (orMask, andMask) ->
            baseOrMask.or(orMask) to andMask
          }
      return { l -> setMasks.map { (orMask, andMask) -> l.or(orMask).and(andMask) } }
    }

    val memory = mutableMapOf<Long, Long>()

    input.lines().drop(1).fold(makeMasks(input.lines().first())) { addressMask, instr ->
      when {
        instr.startsWith("mask = ") -> makeMasks(instr)
        else ->
            extractAddrImm(instr).let { (addr, imm) ->
              memory.putAll(addressMask(addr).associateWith { imm })
              addressMask
            }
      }
    }
    return memory.values.sum()
  }

  @Test
  fun testExample() {
    """
      mask = XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X
      mem[8] = 11
      mem[7] = 101
      mem[8] = 0
    """
        .trimIndent()
        .let { input -> assertEquals(165, part1(input)) }

    """
      mask = 000000000000000000000000000000X1001X
      mem[42] = 100
      mask = 00000000000000000000000000000000X0XX
      mem[26] = 1
    """
        .trimIndent()
        .let { input -> assertEquals(208, part2(input)) }
  }
}
