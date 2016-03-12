package org.ametro.routes.algorithms;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

public class DijkstraHeap {

    public static final long INF = Long.MAX_VALUE / 10;
    public static final int NO_WAY = -1;

    public static Result dijkstra(TransportGraph graph, int start) {

        long[] distances = new long[graph.count];
        int[] predecessors = new int[graph.count];

        Arrays.fill(predecessors, NO_WAY);
        Arrays.fill(distances, INF);
        distances[start] = 0;

        Queue<QueueItem> queue = new PriorityQueue<>();
        queue.add(new QueueItem(0, start));

        while (!queue.isEmpty()) {
            QueueItem currentItem = queue.poll();
            if (currentItem.priority != distances[currentItem.value]) {
                continue;
            }

            for (Edge edge : graph.edges[currentItem.value]) {
                long distance = distances[currentItem.value] + edge.weight;
                if (distances[edge.end] > distance) {
                    distances[edge.end] = distance;
                    predecessors[edge.end] = edge.start;
                    queue.add(new QueueItem(distance, edge.end));
                }
            }
        }
        return new Result(distances, predecessors);
    }

    public static class Result {
        private final long[] distances;
        private final int[] predecessors;

        public Result(long[] distances, int[] predecessors) {
            this.distances = distances;
            this.predecessors = predecessors;
        }

        public long[] getDistances() {
            return distances;
        }

        public int[] getPredecessors() {
            return predecessors;
        }
    }

    public static class TransportGraph {
        public final int count;
        public final EdgeList[] edges;

        public TransportGraph(int count) {
            this.count = count;
            edges = new EdgeList[count];
            for (int i = 0; i < count; i++) {
                edges[i] = new EdgeList();
            }
        }

        public void addEdge(int start, int end, int weight) {
            edges[start].add(new Edge(start, end, weight));
        }

    }

    private static class EdgeList extends ArrayList<Edge> {
    }

    private static class Edge {

        public final int start;
        public final int end;
        public final int weight;

        public Edge(int start, int end, int weight) {
            this.start = start;
            this.end = end;
            this.weight = weight;
        }
    }

    private static class QueueItem implements Comparable<QueueItem> {
        long priority;
        int value;

        public QueueItem(long distance, int value) {
            this.priority = distance;
            this.value = value;
        }

        public int compareTo(@NonNull QueueItem q) {
            return priority < q.priority ? DijkstraHeap.NO_WAY : priority > q.priority ? 1 : 0;
        }
    }

}