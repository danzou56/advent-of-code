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

fun <T> dfs(init: T, getNeighbors: NeighborFunction<T>): Set<T> {
    fun dfs(cur: T, discovered: Set<T>): Set<T> {
        return getNeighbors(cur)
            .filter { v -> v !in discovered }
            .fold(discovered + cur) { acc, v -> acc + dfs(v, acc) }
    }

    return dfs(init, emptySet())
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
                    queue.remove(adjacent)
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