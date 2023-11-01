package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AsciiArt
import dev.danzou.advent.utils.AsciiArtFormat
import dev.danzou.advent22.AdventTestRunner22
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.absoluteValue

internal class Day10 : AdventTestRunner22() {
    data class CpuState(val cycle: Int = 0, val reg: Int = 1) {
        fun apply(instruction: Instruction): CpuState =
            when (instruction) {
                is Instruction.AddX ->
                    CpuState(cycle + instruction.cycles, reg + instruction.value)

                is Instruction.Noop ->
                    CpuState(cycle + instruction.cycles, reg)
            }
    }

    sealed class Instruction(val name: String, val cycles: Int) {
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

    override fun part2(input: String): AsciiArt =
        parse(input).fold(Pair(CpuState(), "")) { (cpuState, crt), instruction ->
            cpuState.apply(instruction).let { newState ->
                Pair(newState, (cpuState.cycle until newState.cycle).fold(crt) { crt, cycle ->
                    crt.plus(
                        if (((cpuState.reg % 40) - (cycle % 40)).absoluteValue <= 1) AsciiArtFormat.DEFAULT_OCCUPIED
                        else AsciiArtFormat.DEFAULT_EMPTY
                    )
                })
            }
        }.second.chunked(40).joinToString("\n").let { art ->
            AsciiArt(art)
        }


    @Test
    fun testExample() {
        val input = readFileString(
            "$baseInputPath/day$day.ex.in"
        )

        val expectedCrt = """
            ##..##..##..##..##..##..##..##..##..##..
            ###...###...###...###...###...###...###.
            ####....####....####....####....####....
            #####.....#####.....#####.....#####.....
            ######......######......######......####
            #######.......#######.......#######.....
        """.trimIndent()

        assertEquals(13140, part1(input))
        assertEquals(expectedCrt, part2(input).art)
    }
}