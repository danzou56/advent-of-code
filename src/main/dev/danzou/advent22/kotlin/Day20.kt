package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Day20 : AdventTestRunner() {

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

        fun insertBefore(value: T, before: Int = 0): Node<T> {
            var cur = this
            var count = 0
            while (count++ < before - 1)
                cur = cur.prev

            val prev = Node(value, cur.prev, cur)
            prev.next.prev = prev
            prev.prev.next = prev
            return prev
        }

        fun remove(): Node<T> {
            if (this.next == this.prev) throw IllegalStateException("Removal of this node empties the list!")
            this.prev.next = this.next
            this.next.prev = this.prev
            return this
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

    override fun part1(input: String): Any {
        val ints = input.split("\n").map { it.toInt() }
        val refs = ints.drop(1).fold(listOf(
            Node.createFirst(ints.first())
        )) { refs, value ->
            refs + refs.last().insertAfter(value)
        }
        refs.forEach { node ->
            when {
                node.data > 0 -> {
                    node.remove()
                    node.next.insertAfter(node.data, node.data)
                }
                node.data < 0 -> {
                    node.remove()
                    node.prev.insertBefore(node.data, -1 * node.data)
                }
            }
        }

        val start = refs.first { it.data == 0 }
        return Node.findAfter(start, 0, 1000) + Node.findAfter(start, 0, 2000) + Node.findAfter(start, 0, 3000)
    }

    override fun part2(input: String): Any {
        TODO("Not yet implemented")
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

        assertEquals(3, part1(input))
        assertEquals(1623178306L, part2(input))
    }
}