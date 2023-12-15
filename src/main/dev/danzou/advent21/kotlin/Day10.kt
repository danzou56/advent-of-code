package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21
import java.util.*

internal class Day10 : AdventTestRunner21() {
    val delims = mapOf(
        '{' to '}',
        '[' to ']',
        '(' to ')',
        '<' to '>',
    )

    val scores = mapOf(
        ')' to 3,       // part 1
        ']' to 57,
        '}' to 1197,
        '>' to 25137,
        '(' to 1,       // part 2
        '[' to 2,
        '{' to 3,
        '<' to 4,
    )

    override fun part1(input: String): Int {
        var corruptScore = 0
        for (line in input.split("\n")) {
            val stack = Stack<Char>()
            for (c in line) {
                when (c) {
                    in delims.keys -> stack.push(c)
                    delims[stack.peek()] -> stack.pop()
                    else -> {
                        assert(delims[stack.pop()] != c)
                        corruptScore += scores[c]!!
                        break
                    }
                }
            }
        }

        return corruptScore
    }

    override fun part2(input: String): Long {
        val scoreList = input.split("\n").map { line ->
            val stack = Stack<Char>()
            for (c in line) {
                when (c) {
                    in delims.keys -> stack.push(c)
                    delims[stack.peek()] -> stack.pop()
                    else -> return@map null
                }
            }
            var score: Long = 0
            while (stack.isNotEmpty()) {
                val popped = stack.pop()!!
                score = score * 5 + scores[popped]!!
            }
            return@map score
        }.filterNotNull().sorted()
        return scoreList[scoreList.size / 2]
    }
}