package rush.units.scout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import battlecode.common.*;
import rush.common.CommonMethods;

public class Scout {
	private static MapLocation moveToLocation;

	public static void start(RobotController rc) {
		Random rng = new Random();
		int maxHeigth = GameConstants.MAP_MAX_HEIGHT;
		int maxWidth = GameConstants.MAP_MAX_WIDTH;

		Team OUR_TEAM = rc.getTeam();
		Team THEIR_TEAM = null;
		if (OUR_TEAM == Team.A)
			THEIR_TEAM = Team.B;
		if (OUR_TEAM == Team.B)
			THEIR_TEAM = Team.A;
		RobotInfo[] sensedenemies;
		RobotInfo target;
		MapLocation temploc;
		temploc = rc.getLocation();
		int counter = 0;
		while (true) {
			if (temploc == rc.getLocation()) {
				counter++;
				if (counter > 10) {
					for (int x = 0; x != 10; x++) {
						int foo = rng.nextInt(4);
						if (rc.canMove(CommonMethods.allDirections()[foo])) {
							try {
								if (rc.hasMoved())
									Clock.yield();
								rc.move(CommonMethods.allDirections()[foo]);
								counter = 0;
								Clock.yield();
							} catch (GameActionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}

			temploc = rc.getLocation();

			sensedenemies = rc.senseNearbyRobots(-1, THEIR_TEAM);
			target = null;

			if (sensedenemies.length != 0) {
				target = findtarget(sensedenemies, rc.getLocation());
				if (rc.canFireSingleShot()) {
					try {
						rc.fireSingleShot(rc.getLocation().directionTo(target.location));
						Clock.yield();
						counter=0;
						continue;
					} catch (GameActionException e) {
						e.printStackTrace();
					}
				}
				// TODO message that you have engaged an (X), and the round
				// number.

				Clock.yield();
				continue;
			}

			// Check for trees
			TreeInfo[] sensedtrees = rc.senseNearbyTrees();
			ArrayList<TreeInfo> treesthatcontaincoins = new ArrayList<TreeInfo>();

			// Do any contain coins?
			for (TreeInfo e : sensedtrees) {
				if (e.containedBullets != 0) {
					treesthatcontaincoins.add(e);
				}
			}

			// Can any of these coin-containing trees be can be shaken already?
			if (treesthatcontaincoins.size() != 0) {
				if (!(rc.canShake()))
					Clock.yield();// If we have already shaken, we should wait
									// until we can shake.
				for (Iterator<TreeInfo> it = treesthatcontaincoins.iterator(); it.hasNext();) {
					TreeInfo e = it.next();
					if (rc.canInteractWithTree(e.ID)) {
						if (!(rc.canShake()))
							Clock.yield();// If we have already shaken, we
											// should wait until we can shake.
						try {
							rc.shake(e.ID);
							it.remove(); // Tree obviously does no longer
											// contain coins, remove it.
						} catch (GameActionException e1) {
							e1.printStackTrace();
						}
					}
				}

			}

			// TODO: Pick closest tree (that we haven't previously pursued) to
			// pursue.

			if (treesthatcontaincoins.size() != 0) {
				TreeInfo selectedTree = treesthatcontaincoins.get(0);
				MapLocation ourlocation = rc.getLocation();
				float distancetoselected = ourlocation.distanceTo(selectedTree.getLocation());
				for (TreeInfo e : treesthatcontaincoins) {
					if (ourlocation.distanceTo(e.getLocation()) < distancetoselected) {
						selectedTree = e;
						distancetoselected = ourlocation.distanceTo(selectedTree.getLocation());
					}
				}

				if (rc.canMove(selectedTree.location)) {
					if (rc.hasMoved()) {
						Clock.yield();
					}
					try {
						rc.move(selectedTree.location);
					} catch (GameActionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					continue;
				}
			}

			// pick random direction, move in that direction. If moving in that
			// direction
			// would move you off the map, pick a new random direction until you
			// can go in that dir.
			if (!(moveToLocation != null)) 
				{
				moveToLocation = new MapLocation(rng.nextFloat() * maxHeigth, rng.nextFloat() * maxWidth);
				}

			
			
			if (!rc.hasMoved()) {
				if (rc.canMove(rc.getLocation().directionTo(moveToLocation)))
					{
					try {
						rc.move(moveToLocation);
					} catch (GameActionException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					} else {						
						moveToLocation = new MapLocation(rng.nextFloat() * maxHeigth, rng.nextFloat() * maxWidth);
					}
			}
			Clock.yield();
			
			//While true
		}
		//Method run end
	}

	private static RobotInfo findtarget(RobotInfo[] sensedenemies, MapLocation ourlocation) {
		RobotInfo target = null;
		for (RobotInfo e : sensedenemies) {
			if (!(target != null)) {
				target = e;
				continue;
			}

			if (e.type == RobotType.ARCHON && target.type == RobotType.ARCHON) {
				if (e.location.distanceTo(ourlocation) < target.location.distanceTo(ourlocation)) {
					target = e;
					continue;
				}
			}

			if (target.type == RobotType.GARDENER || e.type == RobotType.GARDENER) {
				if (e.location.distanceTo(ourlocation) < target.location.distanceTo(ourlocation)) {
					target = e;
					continue;
				}
			}

			if (e.type == RobotType.ARCHON && target.type != RobotType.ARCHON) {
				target = e;
				continue;
			}

			if (e.type == RobotType.GARDENER && target.type != RobotType.GARDENER) {
				if (target.type != RobotType.ARCHON) {
					target = e;
				}
			}

		}

		return target;
	}
}
