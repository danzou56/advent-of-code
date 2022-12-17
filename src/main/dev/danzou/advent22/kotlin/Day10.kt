package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

internal class Day10 : AdventTestRunner() {
    data class CpuState(val cycle: Int = 0, val reg: Int = 1) {
        fun apply(instruction: Instruction): CpuState =
            when (instruction) {
                is Instruction.AddX ->
                    CpuState(cycle + instruction.cycles, reg + instruction.value)
                is Instruction.Noop ->
                    CpuState(cycle + instruction.cycles, reg)
                else -> throw IllegalArgumentException()
            }
    }

    abstract class Instruction(val name: String, val cycles: Int) {
        class AddX(val value: Int) : Instruction("addx", 2)
        class Noop() : Instruction("noop", 1)
    }

    fun parse(input: String): List<Instruction> =
        input.split("\n").map {
            it.split(" ").let {
                when (it.first()) {
                    "addx" -> Instruction.AddX(it.component2().toInt())
                    "noop" -> Instruction.Noop()
                    else -> throw IllegalArgumentException()
                }
            }
        }

    override fun part1(input: String): Any =
        parse(input).fold(Pair(CpuState(), 0)) { (cpuState, sum), instruction ->
            cpuState.apply(instruction).let { newState ->
                if ((newState.cycle - instruction.cycles + 20) % 40 > (newState.cycle + 20) % 40) {
                    Pair(newState, sum + (newState.cycle - (newState.cycle % 40) + 20) * cpuState.reg)
                } else Pair(newState, sum)
            }
        }.second

    override fun part2(input: String): Any =
        parse(input).fold(Pair(CpuState(), "")) { (cpuState, crt), instruction ->
            cpuState.apply(instruction).let { newState ->
                Pair(newState, (cpuState.cycle until newState.cycle).fold(crt) { crt, cycle ->
                    crt + if (((cpuState.reg % 40) - (cycle % 40)).absoluteValue <= 1) "#" else "."
                })
            }
        }.second.chunked(40).joinToString("\n")


    @Test
    fun testExample() {
        val input = """
            addx 15
            addx -11
            addx 6
            addx -3
            addx 5
            addx -1
            addx -8
            addx 13
            addx 4
            noop
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx 5
            addx -1
            addx -35
            addx 1
            addx 24
            addx -19
            addx 1
            addx 16
            addx -11
            noop
            noop
            addx 21
            addx -15
            noop
            noop
            addx -3
            addx 9
            addx 1
            addx -3
            addx 8
            addx 1
            addx 5
            noop
            noop
            noop
            noop
            noop
            addx -36
            noop
            addx 1
            addx 7
            noop
            noop
            noop
            addx 2
            addx 6
            noop
            noop
            noop
            noop
            noop
            addx 1
            noop
            noop
            addx 7
            addx 1
            noop
            addx -13
            addx 13
            addx 7
            noop
            addx 1
            addx -33
            noop
            noop
            noop
            addx 2
            noop
            noop
            noop
            addx 8
            noop
            addx -1
            addx 2
            addx 1
            noop
            addx 17
            addx -9
            addx 1
            addx 1
            addx -3
            addx 11
            noop
            noop
            addx 1
            noop
            addx 1
            noop
            noop
            addx -13
            addx -19
            addx 1
            addx 3
            addx 26
            addx -30
            addx 12
            addx -1
            addx 3
            addx 1
            noop
            noop
            noop
            addx -9
            addx 18
            addx 1
            addx 2
            noop
            noop
            addx 9
            noop
            noop
            noop
            addx -1
            addx 2
            addx -37
            addx 1
            addx 3
            noop
            addx 15
            addx -21
            addx 22
            addx -6
            addx 1
            noop
            addx 2
            addx 1
            noop
            addx -10
            noop
            noop
            addx 20
            addx 1
            addx 2
            addx 2
            addx -6
            addx -11
            noop
            noop
            noop
        """.trimIndent()
        val expectedCrt = """
            ##..##..##..##..##..##..##..##..##..##..
            ###...###...###...###...###...###...###.
            ####....####....####....####....####....
            #####.....#####.....#####.....#####.....
            ######......######......######......####
            #######.......#######.......#######.....
        """.trimIndent()

        assertEquals(13140, part1(input))
        assertEquals(expectedCrt, part2(input))
    }
}