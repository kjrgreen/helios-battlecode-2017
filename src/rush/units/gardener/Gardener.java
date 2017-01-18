package rush.units.gardener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;
import rush.common.CommonMethods;

public class Gardener {
	public static final float PLANTING_DISTANCE = (float) 2.02;// Rounded up
																// from
																// 2.0100something
	static GardenerState state = GardenerState.INITIAL; // Static is okay,
														// because each robot is
														// run on its own JVM.
	static Random rng;

	@SuppressWarnings("unused")
	private static float SENSOR_RAD;
	private static Team OUR_TEAM;
	@SuppressWarnings("unused")
	private static Team THEIR_TEAM;
	private static int ownedtrees;
	@SuppressWarnings("unused")
	private static MapLocation anchorLocation;

	public static void start(RobotController rc) {
		rng = new Random(rc.getRoundNum());
		while (true) {
			System.out.println("My state is:" + state.toString());
			switch (state) {
			case INITIAL:
				initial(rc);
				break;
			case FARMING:
				farming(rc);
				break;
			case POST_FARMING:
				farming(rc);
				break;
			}
		}
	}

	private static void farming(RobotController rc) {
		if (ownedtrees >= 8) {
			state = GardenerState.POST_FARMING;
			return;
		}

		if (rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST && canPlantInAnyDirection(rc)) {
			// TODO path X distance (no more than 1/2 sensor width) from
			// anchorLocation, then build planttreeandrecordit
			while (rc.getLocation().distanceTo(anchorLocation) < 4)
			{
				for(int x = 0; x!=10;x++)
				{
					int foo = rng.nextInt(4);
					if (rc.canMove(CommonMethods.allDirections()[foo])) {
						try {
							rc.move(CommonMethods.allDirections()[foo]);
							Clock.yield();
						} catch (GameActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
			
			//Plant tree
			while (true) {
				int foo = rng.nextInt(4);
				if (rc.canMove(CommonMethods.allDirections()[foo])) {
					if (rc.canPlantTree(CommonMethods.allDirections()[foo])) {
						try {
							planttreeandrecordit(rc, CommonMethods.allDirections()[foo]);
							anchorLocation = rc.getLocation();
							return;
						} catch (Exception e) {
							e.printStackTrace();
							Clock.yield();
						}
					}
					try {
						rc.move(CommonMethods.allDirections()[foo]);
						Clock.yield();
					} catch (GameActionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		// 100 is hardcoded cost of lumberjack
		if (rc.getTeamBullets() >= 80 && canBuildRobotInAnyDirection(rc, RobotType.LUMBERJACK)) {
			try {
				buildRobotInAnyDirection(rc, RobotType.LUMBERJACK);
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}

		TreeInfo targetTree = thirstiestTree(rc.senseNearbyTrees(-1, OUR_TEAM));

		if (rc.canInteractWithTree(targetTree.ID)) {
			while (true) {
				if (rc.canWater()) {
					try {
						rc.water(targetTree.ID);
						return;
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}
				Clock.yield();
			}
		}
		// TODO path to tree, follow path unless something fucks up. Have a
		// counter so that we don't try to get to a tree we cannot reach for too long
		// Hack: move towards it. If you get stuck and can't water it, move randomly. Try to move towards it again. Get stuck? You're SOL.
		while(rc.canMove(targetTree.location))
		{
			try {
				rc.move(targetTree.location);
				Clock.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Clock.yield();
		}
		
		//Same code again, but for good reason. We wanted to water if we were already at the neediest tree. Now we're at it, for certain. Unless it died already. Then we'll just find a new tree or build.
		if (rc.canInteractWithTree(targetTree.ID)) {
			while (true) {
				if (rc.canWater()) {
					try {
						rc.water(targetTree.ID);
						return;
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}
				Clock.yield();
			}
		}
		
		for(int x = 0; x!=4;x++)
		{
			int foo = rng.nextInt(4);
			if (rc.canMove(CommonMethods.allDirections()[foo])) {
				try {
					rc.move(CommonMethods.allDirections()[foo]);
					Clock.yield();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		while(rc.canMove(targetTree.location))
		{
			try {
				rc.move(targetTree.location);
				Clock.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Clock.yield();
		}
		
		//Same code again, but for good reason. We wanted to water if we were already at the neediest tree. Now we're at it, for certain. Unless it died already. Then we'll just find a new tree or build.
		if (rc.canInteractWithTree(targetTree.ID)) {
			while (true) {
				if (rc.canWater()) {
					try {
						rc.water(targetTree.ID);
						return;
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}
				Clock.yield();
			}
		}
	}

	private static TreeInfo thirstiestTree(TreeInfo[] senseNearbyTrees) {
		if (senseNearbyTrees.length == 0)
			return null;
		TreeInfo weakesttree = senseNearbyTrees[0];
		for (TreeInfo e : senseNearbyTrees) {
			if (weakesttree.health > e.health)
				weakesttree = e;
		}
		return weakesttree;
	}

	private static boolean canPlantInAnyDirection(RobotController rc) {
		for (int x = 0; x != 4; x++) {
			if (rc.canPlantTree(CommonMethods.allDirections()[x]))
				return true;
		}
		return false;
	}

	private static boolean canBuildRobotInAnyDirection(RobotController rc, RobotType type) {
		for (int x = 0; x != 4; x++) {
			if (rc.canBuildRobot(type, CommonMethods.allDirections()[x]))
				return true;
		}
		return false;
	}

	private static void buildRobotInAnyDirection(RobotController rc, RobotType type) throws GameActionException {
		for (int x = 0; x != 4; x++) {
			if (rc.canBuildRobot(type, CommonMethods.allDirections()[x])) {
				rc.buildRobot(type, CommonMethods.allDirections()[x]);
				return;
			}
		}
	}

	private static void initial(RobotController rc) {
		SENSOR_RAD = rc.getType().sensorRadius;
		OUR_TEAM = rc.getTeam();
		if (OUR_TEAM == Team.A)
			THEIR_TEAM = Team.B;
		if (OUR_TEAM == Team.B)
			THEIR_TEAM = Team.A;

		// TODO Find clear space to build in/around.
		//Hack: move randomly for 9 turns.
		for(int x = 0; x!=10;x++)
		{
			int foo = rng.nextInt(4);
			if (rc.canMove(CommonMethods.allDirections()[foo])) {
				try {
					rc.move(CommonMethods.allDirections()[foo]);
					Clock.yield();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		while (true) {
			int foo = rng.nextInt(4);
			if (rc.canMove(CommonMethods.allDirections()[foo])) {
				if (rc.canPlantTree(CommonMethods.allDirections()[foo])) {
					try {
						planttreeandrecordit(rc, CommonMethods.allDirections()[foo]);
						anchorLocation = rc.getLocation();
						state = GardenerState.FARMING;
						return;
					} catch (Exception e) {
						e.printStackTrace();
						Clock.yield();
					}
				}
			}
		}
		
	}

	static void planttreeandrecordit(RobotController rc, Direction dir) throws Exception {
		TreeInfo[] tempdiff = rc.senseNearbyTrees(PLANTING_DISTANCE, OUR_TEAM);
		ArrayList<TreeInfo> arrayListtempdiff = new ArrayList<TreeInfo>(Arrays.asList(tempdiff));

		rc.plantTree(dir);

		TreeInfo[] currentsensedtrees = rc.senseNearbyTrees(PLANTING_DISTANCE, OUR_TEAM);
		ArrayList<TreeInfo> arrayListcurrentsensed = new ArrayList<TreeInfo>(Arrays.asList(currentsensedtrees));

		arrayListcurrentsensed.removeAll(arrayListtempdiff);
		if (arrayListcurrentsensed.size() > 1)
			throw new Exception("Built one tree, found more than new trees");
		// System.out.println(arrayListcurrentsensed.get(0));//Debug, show
		// remaining tree
		//ownedtrees.add(arrayListcurrentsensed.get(0));
		ownedtrees++;
	}

}