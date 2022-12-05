package dev.danzou.advent.utils

import java.util.*


class Graph<T>(val verticies: Set<Vertex<T>>, val edges: Map<Vertex<T>, VertexSet<T>>) {
    fun getNeighbors(vertex: Vertex<T>): VertexSet<T> =
        edges.getOrDefault(vertex, emptySet())
}

data class Vertex<T>(val name: T)
typealias VertexSet<T> = Set<Vertex<T>>
typealias NeighborFunction<T> = (T) -> Set<T>

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

/**
 * Computes the shortest path and shortest path cost using Dijkstras's
 * algorithm. It initializes shortestPath with the names of the vertices
 * corresponding to the shortest path. If there is no shortest path,
 * shortestPath will be have entry "None".
 *
 * @param init         vertex to start path at
 * @param target       vertex to end path at
 * @param getNeighbors
 * @param getCost
 * @return Shortest path or empty list if no such path exists
 */
fun <T> doDijkstras(init: T, target: T, getNeighbors: NeighborFunction<T>, getCost: (T, T) -> Int): List<T> {
    /*
     * Initialize cost map so with init having zero cost and all others having
     * undefined cost
    */
    val costs = mutableMapOf(init to 0)

    /*
     * The priority of any given vertex in the queue should be the vertex's
     * cost to travel to it. Each vertex is not necessarily inside the
     * costToVertex map, but the implementation never adds a vertex into the
     * queue before it has a cost.
     */
    val vertexQueue = PriorityQueue(Comparator.comparingInt<T> { costs[it]!! })

    val visited = mutableSetOf<T>()
    val predecessors = mutableMapOf<T, T>()

    /*
     * Keep looking for a path until the start equals end. Once the start
     * equals the end, we've found a path and may return the cost and
     * path.
     */
    var cur = init
    while (cur != target) {
        for (adjacent in getNeighbors(cur)) {
            /*
             * If a vertex is already in visited, then any subsequent visits
             * to it are going to be of higher cost.
             */
            if (!visited.contains(adjacent)) {
                // cur is always in costs map so dereference is safe
                val cost = costs[cur]!! + getCost(cur, adjacent)

                // Update cost if it does not exist or is lower than previous
                if (cost < costs.getOrDefault(adjacent, Int.MAX_VALUE)) {
                    costs[adjacent] = cost
                    predecessors[adjacent] = cur
                    vertexQueue.remove(adjacent)
                    vertexQueue.add(adjacent)
                }
            }
        }
        visited.add(cur)

        /*
         * If the algorithm empties the queue, there exists no path to the
         * end vertex; it has exhausted all adjacent, unvisited vertices.
         * Thus, if the queue is empty, return the expected results for
         * non-existent path. Otherwise, update the start vertex.
         */
        if (vertexQueue.isEmpty()) {
            return emptyList()
        }
        cur = vertexQueue.remove()!!
    }

    // Using the predecessor map, backtrack the shortest path
    var predecessorVertex = target
    val res = mutableListOf(predecessorVertex)
    while (predecessors.containsKey(predecessorVertex)) {
        predecessorVertex = predecessors[predecessorVertex]!!
        res.add(0, predecessorVertex)
    }

    return res
}