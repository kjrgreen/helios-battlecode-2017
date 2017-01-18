package rush.units.archon;

import java.util.ArrayList;
import java.util.Iterator;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import rush.common.CommonMethods;

public class Archon {
	static ArchonState state = ArchonState.INITIAL; //Static is okay, because each robot is run on its own JVM.
	
	private static final int PURSUE_TREES_FOR = 50; // Amount of rounds until giving up on trying to get to a tree.
	
	private static float SENSOR_RAD;
	private static Team OUR_TEAM;
	private static Team THEIR_TEAM;
	private static TreeInfo selectedTree;
	private static int treeSelectedOnRound;
	private static ArrayList<TreeInfo> previouslySelectedTrees = new ArrayList<TreeInfo>();
	
	public static void start(RobotController rc)
	{
		while(true) //Main loop. Note how each state must yield on its own!
		{
			switch (state) {
			case INITIAL:
				initial(rc);
				break;
			case BUILDING:
				building(rc);
				break;
			case COLLECTING:
				collecting(rc);
				break;
//			default:
//				return;//This should never be reached, but is necessary.
			}
		}
	}

	private static void building(RobotController rc) {
		if (rc.getTeamBullets() > 200 && rc.getRoundNum() <= 100)
		{
			for(Direction e : CommonMethods.allDirections()){
				if (rc.canHireGardener(e))
					try {
						rc.hireGardener(e);
						state = ArchonState.COLLECTING;
					} catch (GameActionException e1) {
						System.out.println("I'm a " + rc.getType().toString()
				        		+ " at " + new Float(rc.getLocation().x).toString() + ", " 
				        		+ new Float(rc.getLocation().y).toString()
				        		+ " and I cannot build a gardener. Please debug me! I'm now yielding two turns then exploding. 34984"); //Last few digits should help debugging.
				        Clock.yield();
				        Clock.yield();
				        rc.disintegrate();
					}
			//TODO: Do something to try to find a place to build.
			}
		} else {
			state = ArchonState.COLLECTING;
		}
	}

	private static void collecting(RobotController rc) {
		//Check for enemies.
		RobotInfo[] sensedenemies = rc.senseNearbyRobots(SENSOR_RAD, THEIR_TEAM);
		
		//Are there any enemies?
		if (sensedenemies.length != 0){
			CommonMethods.averageLocation(sensedenemies); //TODO: Use this to move away from the average location of the enemies sensed.
		}
		
		//Are any of those enemies archons? Let your team know.
		for (RobotInfo e : sensedenemies)
		{
			if (e.getType() == RobotType.ARCHON)
			{
				try {
					rc.broadcast(rc.getID(), -1);//I saw an archon!
				} catch (GameActionException e1) {
					e1.printStackTrace();
				} 
			}
			return;//Don't bother with building and collecting if you're running, return here.
		}
		
		//TODO: Pursue already selected tree.
		if (selectedTree != null && (rc.getRoundNum()-treeSelectedOnRound) <= PURSUE_TREES_FOR)
		{
			//Can shake?
			if (rc.canShake(selectedTree.ID))
			{
				try {
					rc.shake(selectedTree.ID);
					treeSelectedOnRound = 0; //No longer pursue this tree.
					return;
				} catch (GameActionException e1) {
					e1.printStackTrace();
					return;
				}
			}
			//TODO: Move towards tree
			return;
		}
		
		//Check for trees
		TreeInfo[] sensedtrees = rc.senseNearbyTrees();
		ArrayList<TreeInfo> treesthatcontaincoins = new ArrayList<TreeInfo>();
		
		//Do any contain coins?
		for (TreeInfo e : sensedtrees)
		{
			if (e.containedBullets != 0)
			{
				treesthatcontaincoins.add(e);
			}
		}
		
		
		//Can any of these coin-containing trees be can be shaken already?
		if (treesthatcontaincoins.size() != 0)
		{
			if (!(rc.canShake())) Clock.yield();//If we have already shaken, we should wait until we can shake.
			for(Iterator<TreeInfo> it = treesthatcontaincoins.iterator(); it.hasNext();){
				TreeInfo e = it.next();
				if (rc.canInteractWithTree(e.ID))
				{
					if (!(rc.canShake())) Clock.yield();//If we have already shaken, we should wait until we can shake.
					try {
						rc.shake(e.ID);
						it.remove(); //Tree obviously does no longer contain coins, remove it.
					} catch (GameActionException e1) {
						e1.printStackTrace();
					}
				}
			}
			
		}
		
		//TODO: Pick closest tree (that we haven't previously pursued) to pursue.
		
		for(Iterator<TreeInfo> it = treesthatcontaincoins.iterator(); it.hasNext();){
			TreeInfo e = it.next();
			if (previouslySelectedTrees.contains(e)) it.remove();//this is a previously pursued tree. ignore it.				
		}
		
		if (treesthatcontaincoins.size() != 0){
			selectedTree = treesthatcontaincoins.get(0);
			treeSelectedOnRound = rc.getRoundNum();
			MapLocation ourlocation = rc.getLocation();
			float distancetoselected = ourlocation.distanceTo(selectedTree.getLocation());
			for(TreeInfo e : treesthatcontaincoins)
			{
				if (ourlocation.distanceTo(e.getLocation()) < distancetoselected){
					selectedTree = e;
					distancetoselected = ourlocation.distanceTo(selectedTree.getLocation());
					}
			}
		}
		
		if (rc.getTeamBullets() > 200)
		{
			for(Direction e : CommonMethods.allDirections()){
				if (rc.canHireGardener(e))
					try {
						rc.hireGardener(e);
						state = ArchonState.COLLECTING;
					} catch (GameActionException e1) {
						System.out.println("I'm a " + rc.getType().toString()
				        		+ " at " + new Float(rc.getLocation().x).toString() + ", " 
				        		+ new Float(rc.getLocation().y).toString()
				        		+ " and I cannot build a gardener. Please debug me! I'm now yielding two turns then exploding. 34984"); //Last few digits should help debugging.
				        Clock.yield();
				        Clock.yield();
				        rc.disintegrate();
					}
			//TODO: Do something to try to find a place to build.
			}
		}
		
		
		//TODO: Wander
	}

	private static void initial(RobotController rc) {
		SENSOR_RAD = rc.getType().sensorRadius;
		OUR_TEAM = rc.getTeam();
		if (OUR_TEAM == Team.A) THEIR_TEAM = Team.B;
		if (OUR_TEAM == Team.B) THEIR_TEAM = Team.A;
		state = ArchonState.BUILDING;		
	}
}