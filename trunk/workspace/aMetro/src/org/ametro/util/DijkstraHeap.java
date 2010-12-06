/*
 * http://code.google.com/p/ametro/
 * Transport map viewer for Android platform
 * Copyright (C) 2009-2010 contacts@ametro.org Roman Golovanov and other
 * respective project committers (see project home page)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 */
package org.ametro.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Queue;

public class DijkstraHeap {
	// input: g - graph, s - start vertex
	// output: prio - distances, pred - predecessors
	public static void dijkstra(Graph g, int s, long[] prio, int[] pred) {
		Arrays.fill(pred, -1);
		Arrays.fill(prio, INF);
		prio[s] = 0;
		Queue<QItem> q = new PriorityQueue<QItem>();
		q.add(new QItem(0, s));
		while (!q.isEmpty()) {
			QItem cur = q.poll();
			if (cur.prio != prio[cur.v]) {
				continue;
			}
			for (Edge e : g.edges[cur.v]) {
				long nprio = prio[cur.v] + e.cost;
				if (prio[e.t] > nprio) {
					prio[e.t] = nprio;
					pred[e.t] = e.s;
					q.add(new QItem(nprio, e.t));
				}
			}
		}
	}

	public static final long INF = Long.MAX_VALUE / 10;

	@SuppressWarnings("serial")
	public static class EdgeList extends ArrayList<Edge> {
		
	}
	
	public static class Graph {
		public final int n;
		public EdgeList[] edges;

		public Graph(int n) {
			this.n = n;
			edges = new EdgeList[n];
			for (int i = 0; i < n; i++) {
				edges[i] = new EdgeList();
			}
		}

		public void addEdge(int s, int t, int cost) {
			edges[s].add(new Edge(s, t, cost));
		}
	}

	public static class Edge {
		public int s, t, cost;

		public Edge(int s, int t, int cost) {
			this.s = s;
			this.t = t;
			this.cost = cost;
		}
	}

	public static class QItem implements Comparable<QItem> {
		long prio;
		int v;

		public QItem(long prio, int v) {
			this.prio = prio;
			this.v = v;
		}

		public int compareTo(QItem q) {
			return prio < q.prio ? -1 : prio > q.prio ? 1 : 0;
		}
	}

}