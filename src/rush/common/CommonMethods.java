package rush.common;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;

public class CommonMethods {
	public static MapLocation averageLocation(MapLocation[] foo) //Returns the averaged location of an array of locations.
	{
		float x = 0;
		float y = 0;
		int count = 0;
		for(MapLocation e : foo)
		{
			x =+ e.x;
			y =+ e.y;
			count++;
		}
		
		return new MapLocation(x/count, y/count);
	}
	
	public static MapLocation averageLocation(RobotInfo[] foo) //Returns the averaged location of an array of locations.
	{
		float x = 0;
		float y = 0;
		int count = 0;
		for(RobotInfo i : foo)
		{
			MapLocation e = i.getLocation();
			x =+ e.x;
			y =+ e.y;
			count++;
		}
		
		return new MapLocation(x/count, y/count);
	}
	
	public static Direction[] allDirections()//Needed because there's no way to legal way to iterate and no values()
	{
		return new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	}
}
