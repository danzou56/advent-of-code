package dev.danzou.advent20.kotlin

import dev.danzou.advent.utils.*
import dev.danzou.advent20.AdventTestRunner20
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day8 : AdventTestRunner20("Handheld Halting") {

  data class Cpu(
      val program: List<Pair<String, Int>>,
      val pc: Int = 0,
      val acc: Int = 0,
      val terminated: Boolean = false,
      val looped: Boolean = false
  ) {
    init {
      require(!(terminated && looped))
    }

    fun process(): Cpu {
      require(!terminated)
      require(!looped)
      require(pc in program.indices)
      val instr = program[pc]
      return when (instr.first) {
        "nop" -> this.copy(pc = pc + 1)
        "jmp" -> this.copy(pc = pc + instr.second)
        "acc" -> this.copy(pc = pc + 1, acc = acc + instr.second)
        else -> throw IllegalArgumentException()
      }
    }

    fun simulate(): Cpu {
      tailrec fun step(cpu: Cpu, seen: Set<Int>): Cpu {
        val next = cpu.process()
        if (next.pc in seen) return cpu.copy(looped = true)
        if (next.pc !in cpu.program.indices) return next.copy(terminated = true)
        return step(next, seen + next.pc)
      }

      return step(this, emptySet())
    }
  }

  fun parseProgram(input: String): List<Pair<String, Int>> =
      input
          .lines()
          .map { it.split(" ") }
          .map { (instr, operand) ->
            instr to
                when (operand.first()) {
                  '-' -> -operand.drop(1).toInt()
                  '+' -> +operand.drop(1).toInt()
                  else -> throw IllegalArgumentException()
                }
          }

  override fun part1(input: String): Any {
    val program = parseProgram(input)
    return Cpu(program).simulate().acc
  }

  override fun part2(input: String): Any {
    val programs =
        parseProgram(input).let { program ->
          program.indices.fold(listOf(program)) { programs, i ->
            programs +
                when (program[i].first) {
                  "nop" -> listOf(program.update(i, "jmp" to program[i].second))
                  "jmp" -> listOf(program.update(i, "nop" to program[i].second))
                  else -> emptyList()
                }
          }
        }

    return programs
        .asSequence()
        .map(::Cpu)
        .map(Cpu::simulate)
        .find { cpu -> cpu.terminated }!!
        .acc
  }

  @Test
  fun testExample() {
    val input =
        """
          nop +0
          acc +1
          jmp +4
          acc +3
          jmp -3
          acc -99
          acc +1
          jmp -4
          acc +6
        """
            .trimIndent()

    assertEquals(5, part1(input))
    assertEquals(8, part2(input))
  }
}
