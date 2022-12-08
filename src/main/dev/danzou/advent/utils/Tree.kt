package dev.danzou.advent.utils

class Node<T>(var children: Set<Node<T>>, val parent: Node<T>?, val data: T) {
    val nodes: Set<Node<T>>
        get() = this.children.map {
            it.nodes
        }.fold(setOf(this)) { a, b -> a + b }

    fun traverse(action: (Node<T>) -> Unit) {
        action(this)
        this.children.forEach { it.traverse(action) }
    }

    fun <R> map(transform: (T) -> R): List<R> {
        throw NotImplementedError()
    }

    fun <R> fold(initial: R, operation: (R, Node<T>) -> R) {
        throw NotImplementedError()
    }

    override fun toString(): String {
        return "${data.toString()}: $children"
    }
}

