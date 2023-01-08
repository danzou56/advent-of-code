package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

internal class Day13 : AdventTestRunner() {

    fun parse(input: String): List<Parser.Packet> =
        input.split("\n").filter { it.isNotEmpty() }.map { Parser.parse(Parser.tokenize(it)) }

    override fun part1(input: String): Any {
        val packets = parse(input)
        return packets.chunked(2).mapIndexed { i, it ->
            if (it.component1() < it.component2()) i + 1
            else 0
        }.sum()
    }

    override fun part2(input: String): Any {
        val two = Parser.Packet.List(Parser.Packet.Value(2), Parser.Packet.Empty)
        val six = Parser.Packet.List(Parser.Packet.Value(6), Parser.Packet.Empty)
        val packets = (parse(input) + listOf(two, six)).sorted()
        return (packets.indexOf(two) + 1) * (packets.indexOf(six) + 1)
    }

    @Test
    fun testExample() {
        val input = """
            [1,1,3,1,1]
            [1,1,5,1,1]

            [[1],[2,3,4]]
            [[1],4]

            [9]
            [[8,7,6]]

            [[4,4],4,4]
            [[4,4],4,4,4]

            [7,7,7,7]
            [7,7,7]

            []
            [3]

            [[[]]]
            [[]]

            [1,[2,[3,[4,[5,6,7]]]],8,9]
            [1,[2,[3,[4,[5,6,0]]]],8,9]
        """.trimIndent()

        val packetPairs = parse(input).chunked(2)
        packetPairs.zip(listOf(true, true, false, true, false, true, false, false)).forEach { (pair, ordered) ->
            if (ordered) assertTrue(pair.component1() < pair.component2())
            else assertTrue(pair.component1() > pair.component2())
        }

        assertEquals(13, part1(input))
    }

    object Parser {
        sealed class Token {
            object LBracketTok : Token()
            object RBracketTok : Token()
            class IntTok(val value: Int) : Token()
            object CommaTok : Token()
        }

        fun tokenize(line: String): List<Token> {
            val toks = mutableListOf<Token>()
            fun step(line: String) {
                if (line.isEmpty()) return
                when (line.first()) {
                    '[' -> toks.add(Token.LBracketTok)
                    ']' -> toks.add(Token.RBracketTok)
                    ',' -> toks.add(Token.CommaTok)
                    else -> {
                        val res = Regex("""^\d+""").find(line) ?: throw IllegalArgumentException()
                        toks.add(Token.IntTok(res.value.toInt()))
                        return step(line.drop(res.value.length))
                    }
                }
                return step(line.drop(1))
            }

            step(line)
            return toks
        }

        sealed class Packet : Comparable<Packet> {
            object Empty : Packet() {
                override fun compareTo(other: Packet): Int =
                    when (other) {
                        is Empty -> 0
                        else -> -1
                    }
            }

            class Value(val value: Int) : Packet() {
                override fun toString(): String {
                    return "${super.toString()}($value)"
                }

                override fun compareTo(other: Packet): Int =
                    when (other) {
                        is Empty -> 1
                        is Value -> this.value.compareTo(other.value)
                        is List -> List(this, Empty).compareTo(other)
                    }
            }

            class List(val first: Packet, val rest: Packet) : Packet() {
                override fun toString(): String {
                    return "${super.toString()}($first, $rest)"
                }

                override fun compareTo(other: Packet): Int =
                    when (other) {
                        is Empty -> 1
                        is Value -> this.compareTo(List(other, Empty))
                        is List -> this.first.compareTo(other.first).let {
                            if (it == 0) this.rest.compareTo(other.rest)
                            else it
                        }
                    }
            }

            override fun toString(): String {
                return this.javaClass.simpleName
            }
        }

        fun parse(toks: List<Token>): Packet {
            val (rest, expr) = parseExpr(toks)
            assert(rest.isEmpty())
            return expr
        }

        fun List<Token>.match(tok: Token): List<Token> = when (this.first()) {
            tok -> this.drop(1)
            else -> throw IllegalArgumentException("Expected token $tok but received ${this.first()}")
        }

        fun parseList(toks: List<Token>): Pair<List<Token>, Packet> =
            when (toks.first()) {
                Token.RBracketTok -> Pair(toks, Packet.Empty)
                else -> parseExpr(toks).let { (rest, headExpr) ->
                    rest.first().let { next ->
                        when (next) {
                            Token.CommaTok -> parseList(rest.match(next)).let { (rest, tailExpr) ->
                                Pair(rest, Packet.List(headExpr, tailExpr))
                            }
                            else -> Pair(rest, Packet.List(headExpr, Packet.Empty))
                        }
                    }
                }
            }

        fun parseExpr(toks: List<Token>): Pair<List<Token>, Packet> =
            toks.first().let { next ->
                when (next) {
                    Token.LBracketTok -> parseList(toks.drop(1)).let { (rest, expr) ->
                        Pair(rest.match(Token.RBracketTok), expr)
                    }
                    is Token.IntTok -> Pair(toks.drop(1), Packet.Value(next.value))
                    else -> throw IllegalArgumentException("Illegal token $next")
                }
            }

    }
}