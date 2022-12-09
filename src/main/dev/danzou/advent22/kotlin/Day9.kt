package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import dev.danzou.advent.utils.minus
import dev.danzou.advent.utils.plus
import dev.danzou.advent.utils.toPair
import kotlin.math.absoluteValue

internal class Day9 : AdventTestRunner() {

    fun simulate(rope: Rope, dir: Pair<Int, Int>): Rope {
        dir.toList().map { it.absoluteValue }.sum().let { sum ->
//            if (sum > 1) println(sum)
        }
        val newHead = rope.head + dir
        return when {
//            (rope.tail - newHead).toList().map { it.absoluteValue }.sum() > 1 -> {
//                Rope(newHead, rope.head + dir.toList().map { it.absoluteValue / it }.toPair())
//            }
            (rope.tail - newHead).toList().map { it.absoluteValue }.max() > 1 -> {
                if (dir.toList().map { it.absoluteValue }.sum() > 1) {
                    Rope(newHead, rope.head + dir.toList().map {
                        if (it == 0) it
                        else it.absoluteValue / it
                    }.toPair())
                } else {
                    Rope(newHead, newHead - dir)
                }
            }
            else -> Rope(newHead, rope.tail)
        }
    }

//    fun getNewTailPosition(rope: Rope, di)
//
    data class Rope(val head: Pair<Int, Int> = Pair(0, 0), val tail: Pair<Int, Int> = Pair(0, 0))

    fun parse(input: String) = input.split("\n")
        .map { it.split(" ").toPair() }
        .map { (dir, amt) -> Pair(
            when (dir) {
                "R" -> Pair(1, 0)
                "L" -> Pair(-1, 0)
                "U" -> Pair(0, 1)
                "D" -> Pair(0, -1)
                else -> throw IllegalStateException()
            },
            amt.toInt()
        ) }

    override fun part1(input: String): Any {
        val res = parse(input)
            .fold(Pair(Rope(), emptySet<Pair<Int, Int>>())) { (rope, visited), (dir, amt) ->
                (0 until amt).fold(Pair(rope, visited)) { (rope, visited), _ ->
                    simulate(rope, dir).let { newRope ->
                        Pair(newRope, visited + newRope.tail)
                    }
                }.also { (rope, _) -> /*println(rope.head)*/ }
            }
        return res.second.size
    }

    override fun part2(input: String): Any {
        val res = parse(input)
            .fold(Pair(
                List(9) { Rope() },
                emptySet<Pair<Int, Int>>()
            )) { (ropes, visited), (dir, amt) ->
                (0 until amt).fold(Pair(ropes, visited)) { (ropes, visited), _ ->
                    ropes.foldIndexed(
                        Pair(emptyList<Rope>(), dir)
                    ) { i, (ropesAcc, dir), rope ->
                        when (dir) {
                            Pair(0, 0) -> Pair(ropesAcc + rope, rope.tail - ropes.getOrElse(i + 1) { ropes[i] }.head)
                            else -> simulate(rope, dir).let { newRope ->
                                Pair(ropesAcc + newRope, newRope.tail - ropes.getOrElse(i + 1, { ropes[i] }).head)
                            }
                        }
                    }.let { (newRopes, _) ->
                        Pair(newRopes, visited + newRopes.last().tail).also {
//                            println(it.first)
                        }
                    }
/*                    ropes.drop(1).fold(
                        listOf(simulate(ropes.first(), dir))
                    ) { ropes, rope ->
                        (rope.head - ropes.last().tail).let { diff ->
                            when (diff) {
                                Pair(0, 0) -> ropes + rope
                                else -> ropes + simulate(rope, diff)
                            }
                        }
                    }.let { newRopes ->
                        Pair(newRopes, visited + newRopes.last().tail)
                    }*/
                }.also {
//                    println(it.first)
                }
            }
        return res.second.size
    }
}


