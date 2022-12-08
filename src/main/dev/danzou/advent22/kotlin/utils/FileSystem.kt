package dev.danzou.advent22.kotlin.utils

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