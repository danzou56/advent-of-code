package dev.danzou.advent21.kotlin

import dev.danzou.advent21.AdventTestRunner21
import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.test.assertEquals
import kotlin.test.assertTrue

//private typealias ParseResult<T> = Pair<List<Day18.Parser.Token>, T>

internal class Day18 : AdventTestRunner21("Snailfish") {

    class SnailfishNumber(val data: SnailfishNode.SnailfishPair) {
        infix operator fun plus(that: SnailfishNumber): SnailfishNumber =
            SnailfishNumber((this.data + that.data).reduce())

        fun magnitude(): Int =
            data.magnitude()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SnailfishNumber

            return data == other.data
        }

        override fun hashCode(): Int {
            return data.hashCode()
        }

        companion object {
            fun fromString(input: String): SnailfishNumber {
                val root = SnailfishNode.fromString(input)
                require(root is SnailfishNode.SnailfishPair)
                return SnailfishNumber(root.reduce())
            }
        }
    }

    sealed class SnailfishNode private constructor(private var _parent: SnailfishPair?) {
        var parent: SnailfishPair?
            get() = _parent
            set(parent) {
                require(parent != null)
                this._parent = parent
                assert(parent != null)
                assert(this._parent != null)
            }

        protected abstract fun findExplosionCandidate(depth: Int = 0): SnailfishPair?
        protected abstract fun findSplitCandidate(): SnailfishNumber?
        abstract fun magnitude(): Int
        abstract fun copy(): SnailfishNode

        companion object {
            fun fromString(input: String): SnailfishNode {
                val tokens = Parser.tokenize(input)
                val root = Parser.parse(tokens) // "root" may not be root
                return root
            }
        }

        class SnailfishNumber(var num: Int, parent: SnailfishPair? = null) : SnailfishNode(parent) {
            fun split(): Boolean {
                require(this.num >= 10)
                val pair = SnailfishPair(
                    SnailfishNumber(floor(this.num / 2.0).toInt()),
                    SnailfishNumber(ceil(this.num / 2.0).toInt())
                )
                pair.left.parent = pair
                pair.right.parent = pair
                this.parent!!.replace(this, pair)
                return true
            }

            override fun findExplosionCandidate(depth: Int): SnailfishPair? = null

            override fun findSplitCandidate(): SnailfishNumber? =
                if (this.num >= 10) this
                else null

            override fun magnitude(): Int = num

            override fun copy(): SnailfishNode =
                SnailfishNumber(num)

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as SnailfishNumber

                return num == other.num
            }

            override fun hashCode(): Int {
                return num
            }

            override fun toString(): String {
                return "Number($num)"
            }

        }

        class SnailfishPair(
            var left: SnailfishNode,
            var right: SnailfishNode,
            parent: SnailfishPair? = null
        ) : SnailfishNode(parent) {

            infix operator fun plus(that: SnailfishPair): SnailfishPair {
                require(parent == null) { "addition is only permitted on the root" }
                val root = SnailfishPair(this.copy(), that.copy())
                root.left.parent = root
                root.right.parent = root
                return root
            }

            fun reduce(): SnailfishPair {
                fun reduce1(): Boolean {
                    var exploded = false
                    while (true)
                        exploded = this.findExplosionCandidate()?.explode() ?: return exploded
                }
                fun reduce2(): Boolean {
                    return this.findSplitCandidate()?.split() ?: false
                }

                while (reduce1() || reduce2());
                return this
            }

            override fun findExplosionCandidate(depth: Int): SnailfishPair? {
                if (depth == 0) require(this.parent == null)
                if (depth > 0) require(this.parent != null)
                require(depth <= 4)

                return if (depth == 4) this
                else left.findExplosionCandidate(depth + 1) ?: right.findExplosionCandidate(depth + 1)
            }

            override fun findSplitCandidate(): SnailfishNumber? =
                left.findSplitCandidate() ?: right.findSplitCandidate()

            override fun magnitude(): Int =
                3 * left.magnitude() + 2 * right.magnitude()

            override fun copy(): SnailfishPair {
                val copy = SnailfishPair(this.left.copy(), this.right.copy())
                copy.left.parent = copy
                copy.right.parent = copy
                return copy
            }

            fun explode(): Boolean {
                require(left is SnailfishNumber)
                require(right is SnailfishNumber)
                val nextOnLeft = findOnLeft()
                if (nextOnLeft != null) nextOnLeft.num += (this.left as SnailfishNumber).num
                val nextOnRight = findOnRight()
                if (nextOnRight != null) nextOnRight.num += (this.right as SnailfishNumber).num

                val INVALID_EXPLODE = "explode only permitted on nodes of exactly depth 4"
                require(this.parent != null) { INVALID_EXPLODE }
                require(this.parent!!.parent != null) { INVALID_EXPLODE }
                require(this.parent!!.parent!!.parent != null) { INVALID_EXPLODE }
                require(this.parent!!.parent!!.parent!!.parent != null) { INVALID_EXPLODE }
                require(this.parent!!.parent!!.parent!!.parent!!.parent == null) { INVALID_EXPLODE }
                this.parent!!.replace(this, SnailfishNumber(0))
                return true
            }

            private fun findOnLeft(): SnailfishNumber? {
                fun descend(cur: SnailfishNode): SnailfishNumber? =
                    when (cur) {
                        is SnailfishNumber -> cur
                        is SnailfishPair -> descend(cur.right)
                    }

                fun ascend(cur: SnailfishNode): SnailfishNumber? {
                    val parent = cur.parent ?: return null
                    if (cur === parent.right) return descend(parent.left)
                    if (cur === parent.left) return ascend(parent)
                    else throw IllegalStateException()
                }

                return ascend(this)
            }

            private fun findOnRight(): SnailfishNumber? {
                fun descend(cur: SnailfishNode): SnailfishNumber? =
                    when (cur) {
                        is SnailfishNumber -> cur
                        is SnailfishPair -> descend(cur.left)
                    }

                fun ascend(cur: SnailfishNode): SnailfishNumber? {
                    val parent = cur.parent ?: return null
                    if (cur === parent.left) return descend(parent.right)
                    if (cur === parent.right) return ascend(parent)
                    else throw IllegalStateException()
                }

                return ascend(this)
            }

            fun replace(node: SnailfishNode, with: SnailfishNode) {
                require(this.left === node || this.right === node)

                with.parent = this
                if (this.left === node) this.left = with
                if (this.right === node) this.right = with
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as SnailfishPair

                if (left != other.left) return false
                if (right != other.right) return false

                return true
            }

            override fun hashCode(): Int {
                var result = left.hashCode()
                result = 31 * result + right.hashCode()
                return result
            }

            override fun toString(): String {
                return "Node($left, $right)"
            }
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

            fun parse(toks: List<Token>): SnailfishNode {
                val (rest, root) = parseExpr(toks)
                assert(rest.isEmpty())

                fun relink(cur: SnailfishNode) {
                    when (cur) {
                        is SnailfishPair -> {
                            cur.left.parent = cur
                            cur.right.parent = cur
                            relink(cur.left)
                            relink(cur.right)
                        }
                        is SnailfishNumber -> {
                            assert(cur.parent != null)
                        }
                    }
                }

                relink(root)
                return root
            }

            fun List<Token>.match(tok: Token): List<Token> = when (this.first()) {
                tok -> this.drop(1)
                else -> throw IllegalArgumentException("Expected token $tok but received ${this.first()}")
            }

            fun parseList(toks: List<Token>): Pair<List<Token>, SnailfishPair> =
                parseExpr(toks).let { (rest, firstExpr) ->
                    rest.match(Token.CommaTok).let { rest ->
                        parseExpr(rest).let { (rest, secondExpr) ->
                            Pair(rest, SnailfishPair(firstExpr, secondExpr))
                        }
                    }
                }

            fun parseExpr(toks: List<Token>): Pair<List<Token>, SnailfishNode> =
                toks.first().let { next ->
                    when (next) {
                        Token.LBracketTok -> parseList(toks.match(Token.LBracketTok)).let { (rest, expr) ->
                            Pair(rest.match(Token.RBracketTok), expr)
                        }
                        is Token.IntTok -> Pair(toks.drop(1), SnailfishNumber(next.value))
                        else -> throw IllegalArgumentException("Illegal token $next")
                    }
                }
        }
    }

    fun parseInput(input: String): List<SnailfishNumber> =
        input.split("\n")
            .map { SnailfishNumber.fromString(it) }

    fun List<SnailfishNumber>.sum() =
        this.reduce(SnailfishNumber::plus)

    override fun part1(input: String): Int =
        input.split("\n")
            .map(SnailfishNumber::fromString)
            .sum()
            .magnitude()

    override fun part2(input: String): Int {
        val snailfishNums = input.split("\n")
            .map(SnailfishNumber::fromString)

        return snailfishNums.flatMapIndexed { i, n1 ->
            snailfishNums.flatMapIndexed { j, n2 ->
                if (i == j) emptyList()
                else try {
                    listOf(n1.plus(n2), n2 + n1)
                } catch (e: Exception) {
                    println(e)
                    throw e
                }
            }
        }.maxOf(SnailfishNumber::magnitude)
    }

    @Test
    fun testAdd() {
        assertEquals(
            SnailfishNumber.fromString("[[1,2],[[3,4],5]]"),
            SnailfishNumber.fromString("[1,2]") + SnailfishNumber.fromString("[[3,4],5]")
        )
    }

    @Test
    fun testExplode() {
        SnailfishNode.fromString("[[[[[9,8],1],2],3],4]").let {
            assertTrue(it is SnailfishNode.SnailfishPair)
            assertEquals(SnailfishNode.fromString("[[[[0,9],2],3],4]"), it.reduce())
        }

        SnailfishNode.fromString("[7,[6,[5,[4,[3,2]]]]]").let {
            assertTrue(it is SnailfishNode.SnailfishPair)
            assertEquals(SnailfishNode.fromString("[7,[6,[5,[7,0]]]]"), it.reduce())
        }

        SnailfishNode.fromString("[[6,[5,[4,[3,2]]]],1]").let {
            assertTrue(it is SnailfishNode.SnailfishPair)
            assertEquals(SnailfishNode.fromString("[[6,[5,[7,0]]],3]"), it.reduce())
        }

        SnailfishNode.fromString("[[3,[2,[1,[7,3]]]],[6,[5,[4,[3,2]]]]]").let {
            assertTrue(it is SnailfishNode.SnailfishPair)
            // SnailfishPair doesn't expose ability to perform one-time explosion
//            assertEquals(SnailfishNode.fromString("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]"), it.reduce())
        }

        SnailfishNode.fromString("[[3,[2,[8,0]]],[9,[5,[4,[3,2]]]]]").let {
            assertTrue(it is SnailfishNode.SnailfishPair)
            assertEquals(SnailfishNode.fromString("[[3,[2,[8,0]]],[9,[5,[7,0]]]]"), it.reduce())
        }
    }

    @Test
    fun testReduce() {
        SnailfishNode.fromString("[[[[[4,3],4],4],[7,[[8,4],9]]],[1,1]]").let {
            assertTrue(it is SnailfishNode.SnailfishPair)
            assertEquals(SnailfishNode.fromString("[[[[0,7],4],[[7,8],[6,0]]],[8,1]]"), it.reduce())
        }
    }

    @Test
    fun testSum() {
        """
            [1,1]
            [2,2]
            [3,3]
            [4,4]
        """.trimIndent().let {
            assertEquals(
                SnailfishNumber.fromString("[[[[1,1],[2,2]],[3,3]],[4,4]]"),
                parseInput(it).sum()
            )
        }

        """
            [1,1]
            [2,2]
            [3,3]
            [4,4]
            [5,5]
        """.trimIndent().let {
            assertEquals(
                SnailfishNumber.fromString("[[[[3,0],[5,3]],[4,4]],[5,5]]"),
                parseInput(it).sum()
            )
        }

        """
            [1,1]
            [2,2]
            [3,3]
            [4,4]
            [5,5]
            [6,6]
        """.trimIndent().let {
            assertEquals(
                SnailfishNumber.fromString("[[[[5,0],[7,4]],[5,5]],[6,6]]"),
                parseInput(it).sum()
            )
        }
    }

    @Test
    fun testLargeSum() {
        parseInput("""
            [[[0,[4,5]],[0,0]],[[[4,5],[2,6]],[9,5]]]
            [7,[[[3,7],[4,3]],[[6,3],[8,8]]]]
            [[2,[[0,8],[3,4]]],[[[6,7],1],[7,[1,6]]]]
            [[[[2,4],7],[6,[0,5]]],[[[6,8],[2,8]],[[2,1],[4,5]]]]
            [7,[5,[[3,8],[1,4]]]]
            [[2,[2,2]],[8,[8,1]]]
            [2,9]
            [1,[[[9,3],9],[[9,0],[0,7]]]]
            [[[5,[7,4]],7],1]
            [[[[4,2],2],6],[8,7]]
        """.trimIndent()).let {
            val expected = parseInput("""
                [[[[4,0],[5,4]],[[7,7],[6,0]]],[[8,[7,7]],[[7,9],[5,0]]]]
                [[[[6,7],[6,7]],[[7,7],[0,7]]],[[[8,7],[7,7]],[[8,8],[8,0]]]]
                [[[[7,0],[7,7]],[[7,7],[7,8]]],[[[7,7],[8,8]],[[7,7],[8,7]]]]
                [[[[7,7],[7,8]],[[9,5],[8,7]]],[[[6,8],[0,8]],[[9,9],[9,0]]]]
                [[[[6,6],[6,6]],[[6,0],[6,7]]],[[[7,7],[8,9]],[8,[8,1]]]]
                [[[[6,6],[7,7]],[[0,7],[7,7]]],[[[5,5],[5,6]],9]]
                [[[[7,8],[6,7]],[[6,8],[0,8]]],[[[7,7],[5,0]],[[5,5],[5,6]]]]
                [[[[7,7],[7,7]],[[8,7],[8,7]]],[[[7,0],[7,7]],9]]
                [[[[8,7],[7,7]],[[8,6],[7,7]]],[[[0,7],[6,6]],[8,7]]]
            """.trimIndent())
            it.drop(1).foldIndexed(it.first()) { i, sum, cur ->
                println(i)
                val actual = sum + cur
                assertEquals(expected[i], actual)
                actual
            }
        }
    }

    @Test
    fun testMagnitude() {
        assertEquals(129, SnailfishNumber.fromString("[[9,1],[1,9]]").magnitude())
    }

    @Test
    fun testExample() {
        val input = """
            [[[0,[5,8]],[[1,7],[9,6]]],[[4,[1,2]],[[1,4],2]]]
            [[[5,[2,8]],4],[5,[[9,9],0]]]
            [6,[[[6,2],[5,6]],[[7,6],[4,7]]]]
            [[[6,[0,7]],[0,9]],[4,[9,[9,0]]]]
            [[[7,[6,4]],[3,[1,3]]],[[[5,5],1],9]]
            [[6,[[7,3],[3,2]]],[[[3,8],[5,7]],4]]
            [[[[5,4],[7,7]],8],[[8,3],8]]
            [[9,3],[[9,9],[6,[4,9]]]]
            [[2,[[7,7],7]],[[5,8],[[9,3],[0,2]]]]
            [[[[5,2],5],[8,[3,7]]],[[5,[7,5]],[4,4]]]
        """.trimIndent()

        assertEquals(4140, part1(input))
    }
}