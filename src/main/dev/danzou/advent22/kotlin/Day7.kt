package dev.danzou.advent22.kotlin

import dev.danzou.advent.utils.Data
import dev.danzou.advent.utils.Node
import dev.danzou.advent.utils.geometry.toPair
import dev.danzou.advent22.AdventTestRunner22

internal class Day7 : AdventTestRunner22() {
    sealed class FileNode(val name: String) : Data() {
        class Dir(name: String) : FileNode(name)
        class File(name: String, val size: Long) : FileNode(name)
    }

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

    fun Node<FileNode>.getSize(): Long =
        when (this.data) {
            is FileNode.Dir -> this.children.sumOf { it.getSize() }
            is FileNode.File -> this.data.size
        }

    override fun part1(input: String): Any {
        val fileTree = buildTree(input)

        return fileTree.nodes
            .filter { it.data is FileNode.Dir }
            .map { it.getSize() }.filter { it < 100_100 }.sum()
    }

    override fun part2(input: String): Any {
        val fileTree = buildTree(input)

        val spaceUsed = fileTree.getSize()
        return fileTree.nodes
            .filter { it.data is FileNode.Dir }
            .map { it.getSize() }
            .filter { it > spaceUsed - 40_000_000 }
            .min()
    }
}
