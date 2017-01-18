package rush;
import battlecode.common.*;
import rush.units.archon.Archon;
import rush.units.gardener.Gardener;
import rush.units.lumberjack.Lumberjack;
import rush.units.scout.Scout;
import rush.units.soldier.Soldier;
import rush.units.tank.Tank;


@SuppressWarnings("unused")
public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        RobotType RobotType = rc.getType();

       switch (RobotType) {
            case ARCHON:
            	System.out.println("I know I'm an archon and will act accordingly.");
                Archon.start(rc);
                break;
            case GARDENER:
                Gardener.start(rc);
                break;
            case SOLDIER:
                Soldier.start(rc);
                break;
            case LUMBERJACK:
                //Lumberjack.start(rc);//TODO UNCOMMENT ME
                //examplefuncsplayer.RobotPlayer.run(rc);//TODO DELETE ME
                break;
            case SCOUT:
            	Scout.start(rc);
            	break;
            case TANK:
            	Tank.start(rc);
            	break;
            
        }
      
        System.out.println("I'm a " + rc.getType().toString()
        		+ " at " + new Float(rc.getLocation().x).toString() + ", " 
        		+ new Float(rc.getLocation().y).toString()
        		+ " and I've escaped from my class-specific code. Please debug me! I'm now yielding two turns then exploding.");
        Clock.yield();
        Clock.yield();
        rc.disintegrate();
	}

}
