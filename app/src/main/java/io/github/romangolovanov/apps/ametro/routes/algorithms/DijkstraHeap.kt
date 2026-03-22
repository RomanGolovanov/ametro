package io.github.romangolovanov.apps.ametro.routes.algorithms

import java.util.PriorityQueue

object DijkstraHeap {

    const val INF: Long = Long.MAX_VALUE / 10
    const val NO_WAY: Int = -1

    fun dijkstra(graph: TransportGraph, start: Int): Result {
        val distances = LongArray(graph.count) { INF }
        val predecessors = IntArray(graph.count) { NO_WAY }
        distances[start] = 0

        val queue = PriorityQueue<QueueItem>()
        queue.add(QueueItem(0L, start))

        while (queue.isNotEmpty()) {
            val currentItem = queue.poll() ?: break
            if (currentItem.priority != distances[currentItem.value]) continue

            for (edge in graph.edges[currentItem.value]) {
                val distance = distances[currentItem.value] + edge.weight
                if (distances[edge.end] > distance) {
                    distances[edge.end] = distance
                    predecessors[edge.end] = edge.start
                    queue.add(QueueItem(distance, edge.end))
                }
            }
        }
        return Result(distances, predecessors)
    }

    class Result(val distances: LongArray, val predecessors: IntArray)

    class TransportGraph(val count: Int) {
        internal val edges: Array<MutableList<Edge>> = Array(count) { mutableListOf() }

        fun addEdge(start: Int, end: Int, weight: Int) {
            edges[start].add(Edge(start, end, weight))
        }
    }

    internal class Edge(val start: Int, val end: Int, val weight: Int)

    internal class QueueItem(val priority: Long, val value: Int) : Comparable<QueueItem> {
        override fun compareTo(other: QueueItem): Int = priority.compareTo(other.priority)
    }
}
