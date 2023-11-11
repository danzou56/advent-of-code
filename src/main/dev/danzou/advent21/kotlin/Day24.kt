package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class Day24 : AdventTestRunner21() {
    data class AluState(val store: Map<Char, Long>) {
        constructor() : this(emptyMap())

        fun advance(instruction: Instruction): AluState =
            this.copy(
                store = this.store + mapOf(
                    instruction.target.identifier to instruction.op(
                        store[instruction.target.identifier] ?: 0,
                        when (instruction.operand) {
                            is Operand.Identifier -> store[instruction.operand.identifier] ?: 0
                            is Operand.Literal -> instruction.operand.number
                        }
                    )
                )
            )
    }

    sealed class Operand {
        data class Identifier(val identifier: Char) : Operand()
        data class Literal(val number: Long) : Operand()
    }

    sealed class Instruction(val op: (Long, Long) -> Long, val target: Operand.Identifier, val operand: Operand) {
        constructor(op: (Long, Long) -> Long, target: String, operand: String) : this(
            op,
            Operand.Identifier(target.first()),
            when {
                operand.first().isLetter() -> Operand.Identifier(operand.first())
                else -> Operand.Literal(operand.toLong())
            }
        )

        class Inp(target: String, inputNumber: Int) : Instruction({ _, _ -> inputNumber.toLong() }, target, target)
        class Add(target: String, operand: String) : Instruction(Long::plus, target, operand)
        class Mul(target: String, operand: String) : Instruction(Long::times, target, operand)
        class Div(target: String, operand: String) : Instruction(Long::div, target, operand)
        class Mod(target: String, operand: String) : Instruction(Long::mod, target, operand)
        class Eql(target: String, operand: String) :
            Instruction({ i1, i2 -> if (i1 == i2) 1L else 0L }, target, operand)
    }

    override fun part1(input: String): Any {
        val cache = mutableMapOf<Pair<Int, AluState>, Optional<List<Int>>>()
        val lines = input.split("\n")

        fun genMonadNumber(index: Int, aluState: AluState, depth: Int = 0): Optional<List<Int>> {
            if (index >= lines.size) {
                return if (aluState.store['z']!! == 0L) Optional.of(emptyList())
                else Optional.empty()
            }
            val rest = index + 1
            val (instr, target, operand) = (lines[index] + " ").split(" ")



            val nextAlu = aluState.advance(when (instr) {
                "add" -> Instruction.Add(target, operand)
                "mul" -> Instruction.Mul(target, operand)
                "div" -> Instruction.Div(target, operand)
                "mod" -> Instruction.Mod(target, operand)
                "eql" -> Instruction.Eql(target, operand)
                else -> {
                    require(instr == "inp")
                    return (9 downTo 1).firstNotNullOfOrNull {
                        if (depth < 3) println("trying $it at depth $depth")
                        if (depth < 2) cache.clear()
                        val nextAlu = aluState.advance(Instruction.Inp(target, it))
//                        if (cache.containsKey(Pair(rest, nextAlu))) println("Cache hit")
                        val tail = cache.getOrPut(Pair(rest, nextAlu)) {
//                            if (cache.size % 1000 == 0) println(cache.size)
                            genMonadNumber(rest, nextAlu, depth + 1)
                        }
                        if (tail.isPresent()) listOf(it) + tail.get()
                        else null
                    }.let { when (it) {
                        null -> Optional.empty()
                        else -> Optional.of(it)
                    } }
                }
            })

            return genMonadNumber(rest, nextAlu, depth)

/*            return lines.first().split(" ").let { instr ->
                when (instr.first()) {
                    "inp" -> (9 downTo 1).firstNotNullOfOrNull {
                        val tail = genMonadNumber(
                            lines.drop(1),
                            aluState.advance(Instruction.Inp(instr.component2(), it))
                        )
                        if (tail.isPresent()) listOf(it) + tail.get()
                        else null
                    }.let {
                        when (it) {
                            null -> Optional.empty()
                            else -> Optional.of(it)
                        }
                    }

                    "add" -> genMonadNumber(lines.drop(1), aluState.advance(Instruction.Add(instr[1], instr[2])))
                    "mul" -> genMonadNumber(lines.drop(1), aluState.advance(Instruction.Mul(instr[1], instr[2])))
                    "div" -> genMonadNumber(lines.drop(1), aluState.advance(Instruction.Div(instr[1], instr[2])))
                    "mod" -> genMonadNumber(lines.drop(1), aluState.advance(Instruction.Mod(instr[1], instr[2])))
                    "eql" -> genMonadNumber(lines.drop(1), aluState.advance(Instruction.Eql(instr[1], instr[2])))
                    else -> TODO()
                }
            }*/
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