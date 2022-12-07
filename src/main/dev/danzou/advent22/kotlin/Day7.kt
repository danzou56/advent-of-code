package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.AdventTestRunner
import dev.danzou.advent.utils.toPair

internal class Day7 : AdventTestRunner() {
    fun buildTree(input: String): Node<FileNode> {
        val fileTree: Node<FileNode> = Node(emptySet(), null, FileNode.Dir("/"))
        var cur = fileTree
        input.split(Regex("\n?\\$ ")).drop(2).forEach { rawLines ->
            val lines = rawLines.split("\n")
            val command = lines.first()
            if (command == "ls") {
                cur.children = lines.drop(1)
                    .map {
                        it.split(" ").toPair().let {
                            when {
                                it.first == "dir" -> FileNode.Dir(it.second)
                                else -> FileNode.File(it.second, it.first.toLong())
                            }
                        }
                    }.map { Node(emptySet(), cur, it) }.toSet()
            } else if (command.split(" ").first() == "cd") {
                val dirName = command.split(" ").last()
                if (dirName == "..") cur = cur.parent!!
                else {
                    cur = cur.children.single { it.data.name == command.split(" ").last() }
                }
            }
        }
        return fileTree
    }


    override fun part1(input: String): Any {
        val fileTree = buildTree(input)

        return traverse(fileTree)
            .filter { it.data is FileNode.Dir }
            .map { it.getSize() }.filter { it < 100_100 }.sum()
    }

    override fun part2(input: String): Any {
        val fileTree = buildTree(input)

        val spaceUsed = fileTree.getSize()
        return traverse(fileTree)
            .filter { it.data is FileNode.Dir }
            .map { it.getSize() }
            .filter { it > spaceUsed - 40_000_000 }
            .min()
    }
}

class Node<T>(var children: Set<Node<T>>, val parent: Node<T>?, val data: T) {
    override fun toString(): String {
        return "${data.toString()}: ${children}"
    }
}

open class FileNode(val name: String) {
    class Dir(name: String) : FileNode(name) {
        override fun toString(): String {
            return "Dir($name)"
        }
    }
    class File(name: String, val size: Long) : FileNode(name) {
        override fun toString(): String {
            return "File($name, $size)"
        }
    }
}

// unused...
enum class Command(val command: String) {
    CD("cd"), LS("ls")
}

fun <T> traverse(root: Node<T>?): Set<Node<T>> {
    if (root == null) return emptySet()
    if (root.children.isEmpty()) return setOf(root)
    return root.children.map {
        traverse(it)
    }.reduce { a, b -> a + b } + root
}

fun Node<FileNode>.getSize(): Long =
    when (this.data) {
        is FileNode.Dir -> this.children.sumOf { it.getSize() }
        is FileNode.File -> this.data.size
        else -> throw NotImplementedError()
    }
