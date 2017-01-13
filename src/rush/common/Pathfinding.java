package rush.common;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;
import battlecode.common.MapLocation;

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
		
		TreeInfo[] trees = rc.senseNearbyTrees();
		
		int dimensions = (int) ((sensorradius*2)/step);
		
		Integer[][] nodes = new Integer[dimensions][dimensions];
		
		int countery = 0;
		int counterx = 0;
		
		MapLocation templocation;
		
		for(float y = bottommost;y <= topmost;y=y+step)
		{
			for(float x = leftmost;x <= topmost;x=x+step)
			{
				templocation = new MapLocation(x, y);
				if(rc.canSenseLocation(templocation))
				{
					nodes[counterx][countery] = 0;
					for(TreeInfo foo : trees)
					{
						if (templocation.isWithinDistance(foo.getLocation(), (foo.getRadius() + bodyradius)))
								{
									//Templocation is not occupiable by this unit; should be weighted accordingly
									break;// !!!IF!!! only one tree can occlude one spot, otherwise add weights for each tree that occludes this node
								}
					}	
				}
				counterx++;
			}
			countery++;
		}
		return null;
	}

}
