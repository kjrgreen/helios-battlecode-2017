package rush.common;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Pathfinding {
	private Set<Vertex> settledNodes;
	private Set<Vertex> unSettledNodes;
	private Map<Vertex, Vertex> predecessors;
	private Map<Vertex, Integer> distance;
	private List<Vertex> nodes;
	private List<Edge> edges;
	private Vertex goal;

	private final int EDGEWEIGHT = 1;  //test value for weigth, may have to be changed

	public MapLocation[] djikstra(RobotController rc, MapLocation destination, float step)
	{
		float sensorRadius = rc.getType().sensorRadius;
		int dimensions = (int) ((sensorRadius*2)/step);
		Vertex[][] nodeArray = computeNodes(dimensions, rc, step, destination);
		computeEdges(dimensions, nodeArray);

		nodes = new ArrayList<Vertex>();

		for(int y = 0; y < dimensions; y++) {
			for(int x = 0; x < dimensions; x++) {
				if(nodeArray[y][x] != null) {
					nodes.add(nodeArray[y][x]);
				}
			}
		}
		int origin =(int) Math.ceil(sensorRadius);
		execute(nodeArray[origin][origin]);
		LinkedList<Vertex> path = getPath(goal);
		MapLocation[] pathArray = new MapLocation[path.size()];
		int index = 0;
		for(Vertex node : path) {
			MapLocation location = node.getLocation();
			pathArray[index] = location;
		}
		return pathArray;
	}
	private Vertex[][] computeNodes(int dimensions, RobotController rc, float step, MapLocation destination) {
		float bodyradius = rc.getType().bodyRadius;
		float sensorradius = rc.getType().sensorRadius;
		float yloc = rc.getLocation().y;
		float xloc = rc.getLocation().x;
		float bottommost = yloc - sensorradius;
		float topmost = yloc + sensorradius;
		float leftmost = xloc - sensorradius;
		float rightmost = xloc + sensorradius;

		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		int counterY = 0;
		int counterX = 0;
		MapLocation nodeLocation;
		Vertex[][] nodes = new Vertex[dimensions][dimensions];

		goal = null;

		for(float y = bottommost;y <= topmost;y=y+step) {
			for(float x = leftmost;x <= rightmost;x=x+step) {
				nodeLocation = new MapLocation(x, y);
				if(rc.canSenseLocation(nodeLocation)) {
					String nodeId = "(" + Integer.toString(counterX) + "," + Integer.toString(counterY) + ")";
					Vertex node = new Vertex(nodeId, nodeLocation, 0);
					nodes[counterX][counterY] = node;
					if (goal == null || goal.getLocation().distanceTo(destination) > nodeLocation.distanceTo(destination))
					{
						goal = node;
					}
					for(TreeInfo tree : nearbyTrees) {
						if (nodeLocation.isWithinDistance(tree.getLocation(), (tree.getRadius() + bodyradius))) {
							//Templocation is not occupiable by this unit; should be weighted accordingly
							node.setWeight(10);
							break;// !!!IF!!! only one tree can occlude one spot, otherwise add weights for each tree that occludes this node
						}
					}
				}
				else {
					nodes[counterX][counterY] = null;
				}
				counterX++;
			}
			counterY++;
		}
		return nodes;
	}

	private void computeEdges(int dimensions, Vertex[][] nodes) {
		edges = new ArrayList<Edge>();
		int edgeCounter = 0;

		for(int y = 0; y < dimensions; y++) {
			for(int x = 0; x < dimensions; y++) {
				if(nodes[y][x] != null) {
					if (y + 1 < dimensions && nodes[y+1][x] != null) {
						String edgeId = Integer.toString(edgeCounter);
						Edge edge = new Edge(edgeId, nodes[y][x], nodes[y + 1][x], EDGEWEIGHT);
						edges.add(edge);
						edgeCounter++;
					}
					if(x + 1 < dimensions && nodes[y][x+1] != null) {
						String edgeId = Integer.toString(edgeCounter);
						Edge edge = new Edge(edgeId, nodes[y][x], nodes[y][x+1], EDGEWEIGHT);
						edges.add(edge);
						edgeCounter++;
					}
				}
			}
		}
	}

	//Following methods have been copied from http://www.vogella.com/tutorials/JavaAlgorithmsDijkstra/article.html
	public void execute(Vertex source) {
		settledNodes = new HashSet<Vertex>();
		unSettledNodes = new HashSet<Vertex>();
		distance = new HashMap<Vertex, Integer>();
		predecessors = new HashMap<Vertex, Vertex>();
		distance.put(source, 0);
		unSettledNodes.add(source);
		while (unSettledNodes.size() > 0) {
			Vertex node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}
	private void findMinimalDistances(Vertex node) {
		List<Vertex> adjacentNodes = getNeighbors(node);
		for (Vertex target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node)
					+ getDistance(node, target)) {
				distance.put(target, getShortestDistance(node)
						+ getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}
	}
	private int getDistance(Vertex node, Vertex target) {
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& edge.getDestination().equals(target)) {
				return edge.getWeight();
			}
		}
		throw new RuntimeException("Should not happen");
	}

	private List<Vertex> getNeighbors(Vertex node) {
		List<Vertex> neighbors = new ArrayList<Vertex>();
		for (Edge edge : edges) {
			if (edge.getSource().equals(node)
					&& !isSettled(edge.getDestination())) {
				neighbors.add(edge.getDestination());
			}
		}
		return neighbors;
	}
	private Vertex getMinimum(Set<Vertex> vertexes) {
		Vertex minimum = null;
		for (Vertex vertex : vertexes) {
			if (minimum == null) {
				minimum = vertex;
			} else {
				if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
					minimum = vertex;
				}
			}
		}
		return minimum;
	}

	private boolean isSettled(Vertex vertex) {
		return settledNodes.contains(vertex);
	}

	private int getShortestDistance(Vertex destination) {
		Integer d = distance.get(destination);
		if (d == null) {
			return Integer.MAX_VALUE;
		} else {
			return d;
		}
	}

	/*
     * This method returns the path from the source to the selected target and
     * NULL if no path exists
     */
	public LinkedList<Vertex> getPath(Vertex target) {
		LinkedList<Vertex> path = new LinkedList<Vertex>();
		Vertex step = target;
		// check if a path exists
		if (predecessors.get(step) == null) {
			return null;
		}
		path.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}
}
