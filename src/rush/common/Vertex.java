package rush.common;

import battlecode.common.MapLocation;

public class Vertex {
    final private String id;
    final private MapLocation location;
    private int weight;

    public Vertex(String id, MapLocation location, int weight) {
        this.id = id;
        this.location = location;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public MapLocation getLocation() {
        return location;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String toString() {
        return id;
    }
}
