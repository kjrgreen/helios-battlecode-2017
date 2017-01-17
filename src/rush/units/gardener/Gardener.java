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
	private static ArrayList<TreeInfo> ownedtrees;
	@SuppressWarnings("unused")
	private static MapLocation anchorLocation;

	public static void start(RobotController rc) {
		rng = new Random(rc.getRoundNum());
		while (true) {
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
		if (ownedtrees.size() >= 8) {
			state = GardenerState.POST_FARMING;
			return;
		}

		if (rc.getTeamBullets() >= GameConstants.BULLET_TREE_COST && canPlantInAnyDirection(rc)) {
			// TODO path X distance (no more than 1/2 sensor width) from
			// anchorLocation, then build planttreeandrecordit
			return;
		}

		// 100 is hardcoded cost of lumberjack
		if (rc.getTeamBullets() >= 100 && canBuildRobotInAnyDirection(rc, RobotType.LUMBERJACK)) {
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

		while (true) {
			int foo = rng.nextInt(4);
			if (rc.canMove(CommonMethods.allDirections()[foo])) {
				if (rc.canBuildRobot(RobotType.LUMBERJACK, CommonMethods.allDirections()[foo])) {
					try {
						rc.buildRobot(RobotType.LUMBERJACK, CommonMethods.allDirections()[foo]);
						state = GardenerState.FARMING;
						break;
					} catch (GameActionException e) {
						e.printStackTrace();
						Clock.yield();
					}
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
		ownedtrees.add(arrayListcurrentsensed.get(0));
	}

}