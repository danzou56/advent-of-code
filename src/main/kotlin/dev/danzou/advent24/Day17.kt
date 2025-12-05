package dev.danzou.advent24

import org.apache.commons.math3.util.ArithmeticUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration

internal class Day17 : AdventTestRunner24("Chronospatial Computer") {

  data class Cpu(
      val instructionPtr: Int,
      val a: Long,
      val b: Long,
      val c: Long,
      val out: Int? = null
  ) {
    fun then(
        instructionPtr: Int = this.instructionPtr + 1,
        a: Long = this.a,
        b: Long = this.b,
        c: Long = this.c,
        out: Int? = null,
    ): Cpu = Cpu(instructionPtr, a, b, c, out)

    fun literal(operand: Long): Long = operand

    fun combo(operand: Long) =
        when (operand) {
          in 0..3 -> operand.toLong()
          4L -> a
          5L -> b
          6L -> c
          else -> throw IllegalArgumentException("Unknown operand: $operand")
        }

    companion object {
      fun fromString(input: String): Cpu {
        val (a, b, c) = input.lines().take(3).map { it.split(": ").last().toLong() }
        return Cpu(
            instructionPtr = 0,
            a = a,
            b = b,
            c = c,
        )
      }
    }
  }

  sealed class Instruction(val opcode: Int, val apply: (Cpu, Long) -> Cpu) {
    /*
      B = A % 8
      B = B + 1
      C = A / 2^B
      A = A / 2^3
      B = B + A
      B = B + C
      out << B % 8

      B = 2B + A + C
     */

    object Adv :
        Instruction(
            0,
            { cpu, operand -> cpu.then(a = cpu.a / twoPow(cpu.combo(operand))) })

    object Bxl :
        Instruction(1, { cpu, operand -> cpu.then(b = cpu.b.xor(cpu.literal(operand))) })

    object Bst : Instruction(2, { cpu, operand -> cpu.then(b = cpu.combo(operand) % 8) })

    object Jnz :
        Instruction(
            3,
            { cpu, operand ->
              cpu.then(
                  instructionPtr =
                      if (cpu.a != 0L) operand.toInt() else cpu.instructionPtr + 1)
            })

    object Bxc : Instruction(4, { cpu, operand -> cpu.then(b = cpu.b.xor(cpu.c)) })

    object Out :
        Instruction(
            5, { cpu, operand -> cpu.then(out = (cpu.combo(operand) % 8).toInt()) })

    object Bdv :
        Instruction(
            6,
            { cpu, operand -> cpu.then(b = cpu.a / twoPow(cpu.combo(operand))) })

    object Cdv :
        Instruction(
            7,
            { cpu, operand -> cpu.then(c = cpu.a / twoPow(cpu.combo(operand))) })

    override fun toString(): String = "${this.javaClass.simpleName}"

    companion object {
      val op: Map<Int, Instruction> =
          listOf(Adv, Bxl, Bst, Jnz, Bxc, Out, Bdv, Cdv).associate { instr ->
            instr.opcode to instr
          }

      fun fromString(input: String): List<Pair<Instruction, Long>> =
          input
              .lines()
              .last()
              .split(": ")
              .last()
              .split(",")
              .map(String::toInt)
              .windowed(2, 2)
              .map { (instruction, operand) ->
                Pair(op[instruction.toInt()]!!, operand.toLong())
              }

      private val powCache = mutableMapOf<Long, Int>()

      private fun twoPow(operand: Long): Int {
        return powCache.getOrPut(operand) { ArithmeticUtils.pow(2, operand) }
      }
    }
  }

  fun run(
      cpu: Cpu,
      instructions: List<Pair<Instruction, Long>>,
      cache: MutableMap<Cpu, List<Int>> = mutableMapOf()
  ): List<Int> {
    val outs = mutableListOf<Int>()
    val subCache = mutableMapOf<Cpu, Int>()

    var cur = cpu
    while (cur.instructionPtr < instructions.size) {
      if (cur in cache) {
        outs.addAll(cache[cur]!!)
        return outs
      }

      val prev = cur
      val (instruction, operand) = instructions[prev.instructionPtr]
      cur = instruction.apply(prev, operand)
      if (cur.out != null) {
        subCache[prev] = outs.size
        outs.add(cur.out)
      }
    }

    cache.putAll(subCache.map { (key, value) -> key to outs.subList(value, outs.size) })
    return outs
  }

  override fun part1(input: String): String {
    val cpu = Cpu.fromString(input)
    val instructions = Instruction.fromString(input)

    return run(cpu, instructions).joinToString(",")
  }

  override fun part2(input: String): Long {
    val cpu = Cpu.fromString(input)
    val instructions = Instruction.fromString(input)
    val program = input.lines().last().split(": ").last().split(",").map(String::toInt)

    fun step(cpu: Cpu, i: Int): Long {
      if (i < 0) return cpu.a.shr(3)
      val lower = (0L..7L).first { lower ->
        val out = run(cpu.copy(a = cpu.a.or(lower)), instructions.dropLast(1))
        out.single() == program[i]
      }
      return step(cpu.copy(a = (cpu.a.or(lower)).shl(3)), i - 1)
    }

    return step(cpu.copy(a = 0), program.size - 1)
  }

  @Test
  fun testExample() {
    """
      Register A: 729
      Register B: 0
      Register C: 0

      Program: 0,1,5,4,3,0
    """
        .trimIndent()
        .let { input -> assertEquals("4,6,3,5,6,3,5,2,1,0", part1(input)) }

    """
      Register A: 117440
      Register B: 0
      Register C: 0
      
      Program: 0,3,5,4,3,0
    """
        .trimIndent()
        .let { input ->
          assertEquals("0,3,5,4,3,0", part1(input))
          assertEquals(117440L, part2(input))
        }
  }
}
