package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day15 : AdventTestRunner23() {

    fun hash(hashable: String): Int = hashable.fold(0) { acc, c -> (17 * (acc + c.code)) % 256 }

    override fun part1(input: String): Any {
        return input.filter { it != '\n' }.split(",").sumOf { hash(it) }
    }

    override fun part2(input: String): Any {
        val instrs = input.filter { it != '\n' }.split(",")
            .map {
                it.takeWhile { it != '-' && it != '=' }.let { hashable ->
                    Triple(hashable, it[hashable.length], it.substringAfter(it[hashable.length]))
                }
            }

        // Woah, mutation????? for some reason, it was just easier to reason about like this today.
        // Default map implementation happens to preserve insertion order!
        val boxes = List(256) { mutableMapOf<String, Int>() }
        instrs.forEach { (hashable, instr, num) ->
            val box = hash(hashable)
            when (instr) {
                '=' -> boxes[box].put(hashable, num.toInt())
                '-' -> boxes[box].remove(hashable)
                else -> throw IllegalArgumentException()
            }
        }

        return boxes.mapIndexed { boxIndex, box ->
            box.entries.mapIndexed { index, (_, num) ->
                (1 + boxIndex) * (1 + index) * num
            }.sum()
        }.sum()
    }

    @Test
    fun testExample() {
        val input = """
            rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
        """.trimIndent()

        assertEquals(145, part2(input))
    }
}