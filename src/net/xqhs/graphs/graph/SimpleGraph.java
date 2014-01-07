/*******************************************************************************
 * Copyright (C) 2013 Andrei Olaru.
 * 
 * This file is part of net.xqhs.Graphs.
 * 
 * net.xqhs.Graphs is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or any later version.
 * 
 * net.xqhs.Graphs is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with net.xqhs.Graphs.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package net.xqhs.graphs.graph;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

import net.xqhs.graphs.pattern.NodeP;
import net.xqhs.graphs.representation.linear.LinearGraphRepresentation;
import net.xqhs.util.logging.Unit;
import net.xqhs.util.logging.UnitComponent;

/**
 * <p>
 * Represents a directed graph, using {@link Node} and {@link Edge} elements.
 * 
 * <p>
 * Functions that modify the graph return the graph itself, so that chained calls are possible.
 * 
 * <p>
 * This class should only be used as a data structure. Visualization should happen elsewhere (for instance, in
 * {@link LinearGraphRepresentation}.
 * 
 * <p>
 * Currently only supports adding of new nodes and edges, as well as reading from / writing to simple formats.
 * 
 * <p>
 * Warning: if a graph contains the edge, id does not necessarily contain any of the nodes of the edge. It may be that
 * the nodes have not been added to the graph. This is because this graph may be a subgraph of a larger graph.
 * 
 * @author Andrei Olaru
 * 
 */
public class SimpleGraph extends Unit implements Graph
{
	/**
	 * The nodes
	 */
	protected Set<Node>	nodes	= null;
	/**
	 * The edges
	 */
	protected Set<Edge>	edges	= null;
	
	/**
	 * Creates an empty graph.
	 */
	public SimpleGraph()
	{
		super();
		nodes = new HashSet<Node>();
		edges = new HashSet<Edge>();
	}
	
	@Override
	public String getUnitName()
	{
		return super.getUnitName();
	}
	
	// FIXME: this does not stop different Node instances with the same label from being added
	@Override
	public SimpleGraph addNode(Node node)
	{
		if(node == null)
			throw new IllegalArgumentException("null nodes not allowed");
		if(!nodes.add(node))
			lw("node [" + node.toString() + "] already present.");
		return this;
	}
	
	/**
	 * Warning: the function will not add the nodes to the graph, only the edge between them. Nodes must be added
	 * separately.
	 * 
	 * @param edge
	 *            : the edge to add
	 * @return the updated graph
	 */
	@Override
	public SimpleGraph addEdge(Edge edge)
	{
		if(edge == null)
			throw new IllegalArgumentException("null edges not allowed");
		if(!edges.add(edge))
			lw("edge [" + edge.toString() + "] already present.");
		return this;
	}
	
	@Override
	public SimpleGraph removeNode(Node node)
	{
		if(!nodes.remove(node))
			lw("node[" + node + "] not contained");
		return this;
	}
	
	@Override
	public SimpleGraph removeEdge(Edge edge)
	{
		if(!edges.remove(edge))
			lw("edge [" + edge + "] not contained");
		return this;
	}
	
	@Override
	public int n()
	{
		return nodes.size();
	}
	
	@Override
	public int m()
	{
		return edges.size();
	}
	
	@Override
	public int size()
	{
		return n();
	}
	
	@Override
	public Collection<Node> getNodes()
	{
		return nodes;
	}
	
	@Override
	public Collection<Edge> getEdges()
	{
		return edges;
	}
	
	@Override
	public boolean contains(Node node)
	{
		return nodes.contains(node);
	}
	
	@Override
	public boolean contains(Edge e)
	{
		return edges.contains(e);
	}
	
	@Override
	public Collection<Node> getNodesNamed(String name)
	{
		Collection<Node> ret = new HashSet<Node>();
		for(Node node : nodes)
			if(node.getLabel().equals(name))
				ret.add(node);
		return ret;
	}
	
	/**
	 * Checks if the inEdges and outEdges lists of the nodes are coherent with the edges of the graph.
	 * 
	 * @return true if the graph is coherent with respect to the above principle.
	 */
	@SuppressWarnings("static-method")
	public boolean isCoherent()
	{
		// TODO
		return true;
	}
	
	/**
	 * Simple Dijkstra algorithm to compute the distance between one node and all others.
	 * 
	 * @param node
	 *            : the source node.
	 * @return the distances to the other nodes.
	 */
	public Map<Node, Integer> computeDistancesFromUndirected(Node node)
	{
		if(!nodes.contains(node))
			throw new IllegalArgumentException("node " + node + " is not in graph");
		if(!(node instanceof ConnectedNode))
			throw new IllegalArgumentException("node " + node + " is not a ConnectedNode");
		Map<Node, Integer> dists = new HashMap<Node, Integer>();
		Queue<ConnectedNode> grayNodes = new LinkedList<ConnectedNode>();
		Set<ConnectedNode> blackNodes = new HashSet<ConnectedNode>();
		grayNodes.add((ConnectedNode) node);
		dists.put(node, new Integer(0));
		
		while(!grayNodes.isEmpty())
		{
			ConnectedNode cNode = grayNodes.poll();
			int dist = dists.get(cNode).intValue();
			blackNodes.add(cNode);
			
			for(Edge e : cNode.getOutEdges())
				if(!blackNodes.contains(e.getTo()))
				{
					if(!grayNodes.contains(e.getTo()))
					{
						if(!(e.getTo() instanceof ConnectedNode))
							throw new IllegalArgumentException("node " + e.getTo() + " is not a ConnectedNode");
						grayNodes.add((ConnectedNode) e.getTo());
					}
					if(!dists.containsKey(e.getTo()) || (dists.get(e.getTo()).intValue() > (dist + 1)))
						dists.put(e.getTo(), new Integer(dist + 1));
				}
			for(Edge e : cNode.getInEdges())
				if(!blackNodes.contains(e.getFrom()))
				{
					if(!grayNodes.contains(e.getFrom()))
					{
						if(!(e.getFrom() instanceof ConnectedNode))
							throw new IllegalArgumentException("node " + e.getFrom() + " is not a ConnectedNode");
						grayNodes.add((ConnectedNode) e.getFrom());
					}
					if(!dists.containsKey(e.getFrom()) || (dists.get(e.getFrom()).intValue() > (dist + 1)))
						dists.put(e.getFrom(), new Integer(dist + 1));
				}
		}
		
		return dists;
	}
	
	/**
	 * Returns a display of the graph that shows the number of nodes and edges, the list of nodes and the list of edges.
	 */
	@Override
	public String toString()
	{
		String ret = "";
		ret += "G[" + n() + ", " + m() + "] ";
		List<Node> list = new ArrayList<Node>(nodes);
		Collections.sort(list, new NodeAlphaComparator());
		ret += list.toString();
		for(Edge e : edges)
			ret += "\n" + e.toString();
		return ret;
	}
	
	/**
	 * Creates a representation of the {@link Graph} in DOT format.
	 * <p>
	 * See <a href = 'http://en.wikipedia.org/wiki/DOT_language'>http://en.wikipedia.org/wiki/DOT_language</a>
	 * 
	 * @return the DOT representation
	 */
	// FIXME: override this method in GraphPattern to handle NodeP instances, instead of doing that here
	public String toDot()
	{
		String ret = "digraph G {\n";
		for(Edge edge : edges)
		{
			String fromNode = edge.getFrom().toString();
			String toNode = edge.getTo().toString();
			// if(fromNode.contains(" "))
			// fromNode = fromNode.replace(' ', '_');
			// if(toNode.contains(" "))
			// toNode = toNode.replace(' ', '_');
			ret += "\t";
			ret += "\"" + fromNode + "\"";
			ret += " -> ";
			ret += "\"" + toNode + "\"";
			if(edge.getLabel() != null)
				ret += " [" + "label=\"" + edge.getLabel() + "\"]";
			ret += ";\n";
		}
		for(Node node : nodes)
		{
			if(node instanceof NodeP && ((NodeP) node).isGeneric())
				ret += "\t\"" + node.toString() + "\" [label=\"" + node.getLabel() + "\"];\n";
			// if(node.getLabel().contains(" "))
			// ret += "\t" + node.getLabel().replace(' ', '_') + " [label=\"" + node.getLabel() + "\"];\n";
		}
		ret += "}";
		return ret;
	}
	
	/**
	 * Reads the structure of the graph as list of edges, adding all nodes appearing in the definition of edges.
	 * <p>
	 * The newly read edges and nodes are added on the existing structure, if any.
	 * 
	 * @param input
	 *            - a stream to read from
	 * @return the enriched {@link SimpleGraph} instance
	 */
	public SimpleGraph readFrom(InputStream input)
	{
		UnitComponent log = (UnitComponent) new UnitComponent().setLink(getUnitName());
		// .setUnitName("test").setLogLevel(Level.ALL);
		Scanner scan = new Scanner(input);
		while(scan.hasNextLine())
		{
			String line = scan.nextLine();
			String edgeReads[] = line.split(";");
			for(String edgeRead : edgeReads)
			{
				log.lf("new edge: " + edgeRead);
				
				String[] parts1 = edgeRead.split("-", 2);
				if(parts1.length < 2)
				{
					log.le("input corrupted");
					continue;
				}
				String node1name = parts1[0].trim();
				String node2name = null;
				String edgeName = null;
				boolean bidirectional = false;
				String[] parts2 = parts1[1].split(">");
				if((parts2.length < 1) || (parts2.length > 2))
				{
					log.le("input corrupted");
					continue;
				}
				
				Node node1 = null;
				Node node2 = null;
				
				if(parts2.length == 2)
				{
					edgeName = parts2[0].trim();
					node2name = parts2[1].trim();
				}
				else
				{
					bidirectional = true;
					parts2 = parts1[1].split("-");
					if(parts2.length == 2)
					{
						edgeName = parts2[0].trim();
						node2name = parts2[1].trim();
					}
					else
						node2name = parts2[0].trim();
				}
				if((edgeName != null) && (edgeName.length() == 0))
					edgeName = null;
				// log.trace("[" + parts1.toString() + "] [" + parts2.toString() + "]");
				log.lf("[" + node1name + "] [" + node2name + "] [" + edgeName + "]");
				
				if(getNodesNamed(node1name).isEmpty())
				{
					node1 = new SimpleNode(node1name);
					addNode(node1);
				}
				else
					node1 = getNodesNamed(node1name).iterator().next();
				
				if(getNodesNamed(node2name).isEmpty())
				{
					node2 = new SimpleNode(node2name);
					addNode(node2);
				}
				else
					node2 = getNodesNamed(node2name).iterator().next();
				
				addEdge(new SimpleEdge(node1, node2, edgeName));
				if(bidirectional)
					addEdge(new SimpleEdge(node2, node1, edgeName));
			}
		}
		scan.close();
		return this;
	}
}