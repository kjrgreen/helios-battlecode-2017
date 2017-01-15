package rush.common;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;

public class Pathfinding {
	public static Direction[] djikstra(RobotController rc, MapLocation destination, float step)
	{
		float bodyradius = rc.getType().bodyRadius;
		
		float sensorradius = rc.getType().sensorRadius;
		float yloc = rc.getLocation().y;
		float xloc = rc.getLocation().x;
		
		float bottommost = yloc - sensorradius;
		float topmost = yloc + sensorradius;
		float leftmost = xloc - sensorradius;
		float rightmost = xloc + sensorradius;
		
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees();
		
		int dimensions = (int) ((sensorradius*2)/step);

		Vertex[][] nodes = new Vertex[dimensions][dimensions];

		int counterY = 0;
		int counterX = 0;
		
		MapLocation nodeLocation;
		
		for(float y = bottommost;y <= topmost;y=y+step)
		{
			for(float x = leftmost;x <= rightmost;x=x+step)
			{
				nodeLocation = new MapLocation(x, y);
				if(rc.canSenseLocation(nodeLocation))
				{
					String nodeId = "(" + Integer.toString(counterX) + "," + Integer.toString(counterY) + ")";
					Vertex node = new Vertex(nodeId, nodeLocation, 0);
					nodes[counterX][counterY] = node;
					for(TreeInfo tree : nearbyTrees)
					{
						if (nodeLocation.isWithinDistance(tree.getLocation(), (tree.getRadius() + bodyradius)))
						{
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

		int numberOfEdges = 3 * dimensions * dimensions - 6 * dimensions + 2;

		Edge[] edges = new Edge[numberOfEdges];
		int edgeCounter = 0;

		for(int y = 0; y < dimensions; y++) {
			for(int x = 0; x < dimensions; y++) {
				
			}
		}

		return null;
	}

}
