package dev.danzou.advent.utils

import java.util.*


class Graph<T>(val verticies: Set<Vertex<T>>, val edges: Map<Vertex<T>, VertexSet<T>>) {
    fun getNeighbors(vertex: Vertex<T>): VertexSet<T> =
        edges.getOrDefault(vertex, emptySet())
}

data class Vertex<T>(val name: T)
typealias VertexSet<T> = Set<Vertex<T>>
typealias NeighborFunction<T> = (T) -> Set<T>
typealias CostFunction<T> = (T, T) -> Int

/**
 * Perform depth first search, returning all discovered nodes. May stack overflow for very large
 * graphs; if this is undesirable, use bfs
 */
fun <T> dfs(init: T, getNeighbors: NeighborFunction<T>): Set<T> {
    val stack = Stack<T>()
    val discovered = mutableSetOf<T>()
    stack.push(init)
    while (stack.isNotEmpty()) {
        val vertex = stack.pop()
        if (vertex !in discovered) {
            discovered.add(vertex)
            for (adjacent in getNeighbors(vertex)) {
                stack.push(adjacent)
            }
        }
    }

    return discovered
}

/**
 * Perform breadth first search, returning all discovered nodes
 */
fun <T> bfs(init: T, getNeighbors: NeighborFunction<T>): Set<T> {
    val queue: Queue<T> = LinkedList()
    val discovered = mutableSetOf(init)
    queue.add(init)
    while (queue.isNotEmpty()) {
        val cur = queue.poll()!!
        for (adjacent in getNeighbors(cur)) {
            if (adjacent !in discovered) {
                discovered.add(adjacent)
                queue.add(adjacent)
            }
        }
    }
    return discovered
}

/**
 * Perform breadth first search, returning all discovered nodes and their shortest path
 */
fun <T> findAllPaths(init: T, getNeighbors: NeighborFunction<T>): Set<List<T>> {
    val queue: Queue<T> = LinkedList()
    val discovered = mutableMapOf(init to listOf(init))
    queue.add(init)
    while (queue.isNotEmpty()) {
        val cur = queue.poll()!!
        for (adjacent in getNeighbors(cur)) {
            if (adjacent !in discovered) {
                discovered[adjacent] = discovered[cur]!! + adjacent
                queue.add(adjacent)
            }
        }
    }
    return discovered.values.toSet()
}

fun <T> findPaths(init: T, target: T, getNeighbors: NeighborFunction<T>): Set<List<T>> {
    fun findPaths(cur: T, path: List<T>): Set<List<T>> {
        if (cur == target) return setOf(path + cur)
        return getNeighbors(cur)
            .filter { v -> v !in path }
            .map { v -> findPaths(v, path + cur) }
            .flatten()
            .toSet()
    }

    return findPaths(init, emptyList())
}

fun <T> findPaths(init: T, target: T, getNeighbors: (T, List<T>) -> Set<T>): Set<List<T>> {
    fun findPaths(cur: T, path: List<T>): Set<List<T>> {
        if (cur == target) return setOf(path + cur)
        return getNeighbors(cur, path)
//            .filter { v -> v !in path }
            .map { v -> findPaths(v, path + cur) }
            .flatten()
            .toSet()
    }

    return findPaths(init, emptyList())
}

/**
 * Computes the shortest path and shortest path cost using Dijkstras's
 * algorithm, returning that path, or an empty list or no such path exists
 *
 * @param init         vertex to start path at
 * @param target       vertex to end path at
 * @param getNeighbors
 * @param getCost
 * @return Shortest path or empty list if no such path exists
 */
fun <T> doDijkstras(
    init: T,
    target: (T) -> Boolean,
    getNeighbors: NeighborFunction<T>,
    getCost: (T, T) -> Int = { _, _ -> 1 }
): List<T> {
    val costs = mutableMapOf(init to 0)
    // vertex is always in cost map so dereference is safe
    val queue = PriorityQueue(Comparator.comparingInt<T> { costs[it]!! })

    val visited = mutableSetOf<T>()
    val predecessors = mutableMapOf<T, T>()

    var cur = init
    while (!target(cur)) {
        for (adjacent in getNeighbors(cur)) {
            // Second visits are always the same or higher cost
            if (!visited.contains(adjacent)) {
                // cur is always in costs map so dereference is safe
                val cost = costs[cur]!! + getCost(cur, adjacent)

                if (cost < (costs[adjacent] ?: Int.MAX_VALUE)) {
                    costs[adjacent] = cost
                    predecessors[adjacent] = cur
                    queue.add(adjacent)
                }
            }
        }
        visited.add(cur)

        // If queue is emptied, there exists no path to the end vertex
        cur = queue.poll() ?: return emptyList()
    }

    // Backtrack the shortest path
    val res = mutableListOf(cur)
    while (predecessors.containsKey(cur)) {
        cur = predecessors[cur]!!
        res.add(0, cur)
    }

    return res
}