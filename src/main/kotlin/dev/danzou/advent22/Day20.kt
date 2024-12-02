package dev.danzou.advent22

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Day20 : AdventTestRunner22() {

    val KEY = 811589153L

    class Node<T> private constructor(val data: T) {
        private var _prev: Node<T>? = null
        private var _next: Node<T>? = null
        var prev: Node<T>
            get() = _prev!!
            set(value) {
                _prev = value
            }
        var next: Node<T>
            get() = _next!!
            set(value) {
                _next = value
            }

        constructor(data: T, prev: Node<T>, next: Node<T>) : this(data) {
            this._prev = prev
            this._next = next
        }

        fun move(amount: Int) = when {
            amount > 0 -> moveForward(amount)
            amount < 0 -> moveBackward(amount * -1)
            else -> Unit
        }

        private fun moveForward(amount: Int) {
            var cur = this
            var count = 0
            while (count++ < amount)
                cur = cur.next

            this.prev.next = this.next
            this.next.prev = this.prev

            this.next = cur.next
            this.prev = cur
            cur.next.prev = this
            cur.next = this
        }

        private fun moveBackward(amount: Int) {
            var cur = this
            var count = 0
            while (count++ < amount)
                cur = cur.prev

            this.prev.next = this.next
            this.next.prev = this.prev

            this.prev = cur.prev
            this.next = cur
            cur.prev.next = this
            cur.prev = this

        }

        fun insertAfter(value: T, after: Int = 0): Node<T> {
            var cur = this
            var count = 0
            while (count++ < after - 1)
                cur = cur.next

            val next = Node(value, cur, cur.next)
            next.prev.next = next
            next.next.prev = next
            return next
        }

        override fun toString(): String {
            return "Node(data=$data)"
        }

        companion object {
            fun <T> createFirst(data: T) : Node<T> {
                val node = Node(data)
                node.prev = node
                node.next = node
                return node
            }

            fun <T> findAfter(init: Node<T>, value: T, after: Int = 0): T {
                var cur = init
                while (cur.data != value)
                    cur = cur.next

                assert(cur.data == value)
                var count = 0
                while (count ++ < after)
                    cur = cur.next

                return cur.data
            }
        }
    }

    fun mix(refs: List<Node<Long>>) {
        val mod = refs.size - 1
        refs.forEach {
            // correctly choosing the direction to move the node halves
            // execution time
            // val distance = (((it.data % mod) + mod + mod / 2) % mod) - mod / 2
            val distance = it.data % mod
            it.move(distance.toInt())
        }
    }

    override fun part1(input: String): Any {
        val longs = input.split("\n").map { it.toLong() }
        val refs = longs.drop(1).fold(listOf(
          Node.createFirst(longs.first())
        )) { refs, value ->
            refs + refs.last().insertAfter(value)
        }
        mix(refs)

        val start = refs.first { it.data == 0L }
        return Node.findAfter(start, 0L, 1000) + Node.findAfter(start, 0L, 2000) + Node.findAfter(start, 0L, 3000)
    }

    override fun part2(input: String): Any {
        val longs = input.split("\n").map { it.toInt() * KEY }
        val refs = longs.drop(1).fold(listOf(
          Node.createFirst(longs.first())
        )) { refs, value ->
            refs + refs.last().insertAfter(value)
        }

        (0 until 10).forEach { mix(refs) }

        val start = refs.first { it.data == 0L }
        return Node.findAfter(start, 0, 1000) + Node.findAfter(start, 0, 2000) + Node.findAfter(start, 0, 3000)
    }

    @Test
    fun testExample() {
        val input = """
            1
            2
            -3
            3
            -2
            0
            4
        """.trimIndent()

        assertEquals(3L, part1(input))
        assertEquals(1623178306L, part2(input))
    }
}