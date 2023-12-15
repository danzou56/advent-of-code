package dev.danzou.advent23.kotlin

import dev.danzou.advent23.AdventTestRunner23
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class Day15 : AdventTestRunner23("Lens Library") {

    fun hash(hashable: String): Int = hashable.fold(0) { acc, c -> (17 * (acc + c.code)) % 256 }

    override fun part1(input: String): Int {
        return input.split(",").sumOf(::hash)
    }

    override fun part2(input: String): Int {
        val instrs = input.split(",").map { it.split('-', '=') }

        // Woah, mutation??? The solution is much easier to read as mutating maps inside the
        // random-access list versus folding over the instructions with a list maps. Even better,
        // the default kotlin map implementation preserves insertion order. When we go to find the
        // slot of each lens, we can iterate through the entry set under that assumption.
        val boxes = List(256) { mutableMapOf<String, Int>() }
        instrs.forEach { (hashable, num) ->
            val box = hash(hashable)
            when (num) {
                "" -> boxes[box].remove(hashable)
                else -> boxes[box].put(hashable, num.toInt())
            }
        }

        return boxes.mapIndexed { box, lenses ->
            lenses.entries.mapIndexed { slot, (_, focalLength) ->
                (1 + box) * (1 + slot) * focalLength
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