package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class Day24 : AdventTestRunner21() {
    data class AluState(val store: Map<Char, Long>) {
        constructor() : this(mapOf('w' to 0, 'x' to 0, 'y' to 0, 'z' to 0))

        inline fun advance(op: (Long, Long) -> Long, target: String, operand: String): AluState {
            return this.copy(
                store = this.store + mapOf(
                    target.first() to op(
                        store[target.first()]!!,
                        when {
                            operand.first().isLetter() -> store[operand.first()]!!
                            else -> operand.toLong()
                        }
                    )
                )
            )
        }
            
    }

    override fun part1(input: String): Any {
        val cache = mutableMapOf<Pair<Int, AluState>, Optional<List<Int>>>()
        val lines = input.split("\n")

        /*tailrec*/ fun consumeInstrs(index: Int, aluState: AluState): Pair<Int, AluState> {
            if (index >= lines.size) return Pair(index, aluState)
            val (instr, target, operand) = (lines[index] + " ").split(" ")

            return consumeInstrs(
                index + 1,
                aluState.advance(when (instr) {
                    "add" -> Long::plus
                    "mul" -> Long::times
                    "div" -> Long::div
                    "mod" -> { l1: Long, l2: Long -> l1.mod(l2) }
                    "eql" -> { l1: Long, l2: Long -> if (l1 == l2) 1L else 0L }
                    else -> return Pair(index, aluState)
                }, target, operand)
            )
        }

        fun genMonadNumber(index: Int, aluState: AluState, depth: Int = 0): Optional<List<Int>> {
            val (curIndex, curAlu) = consumeInstrs(index, aluState)
            if (curIndex >= lines.size) {
                return if (curAlu.store['z']!! == 0L) Optional.of(emptyList())
                else Optional.empty()
            }
            val rest = curIndex + 1
            val (instr, target, operand) = (lines[curIndex] + " ").split(" ")

            require(instr == "inp")
            return (9 downTo 1).firstNotNullOfOrNull {
                if (depth < 3) println("trying $it at depth $depth")
                if (depth < 2) cache.clear()
                val nextAlu = curAlu.advance(
                    { _, l2 -> l2 },
                    target,
                    it.toString()
                )
                val tail = cache.getOrPut(Pair(rest, nextAlu)) {
                    genMonadNumber(rest, nextAlu, depth + 1)
                }
                if (tail.isPresent) listOf(it) + tail.get()
                else null
            }.let { when (it) {
                null -> Optional.empty()
                else -> Optional.of(it)
            } }
        }

        return genMonadNumber(
            0,
            AluState()
        ).get().joinToString("").toLong()
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
    }

    @Test
    fun testSmallNums() {
        val input = """
            inp y
            inp x
            add w x
            mod x 3
            mod w 2
            eql x 0
            eql w 0
            eql x w
            mul w 0
            add x -1
            add z x
            mul z 1
        """.trimIndent()

        assertEquals(97L, part1(input))
    }

}