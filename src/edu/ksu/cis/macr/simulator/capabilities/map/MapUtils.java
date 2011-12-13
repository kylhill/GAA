package edu.ksu.cis.macr.simulator.capabilities.map;

import java.util.Random;

import edu.ksu.cis.macr.simulator.agent.Direction;
import edu.ksu.cis.macr.simulator.capabilities.LocationData;

/**
 * A collection of map utility functions
 * 
 * @author Kyle Hill
 * 
 */
public final class MapUtils {
    /**
     * Constructor
     */
    private MapUtils() {
        // Prevent Instantiation
    }

    /**
     * Get the direction from the first location to the second
     * 
     * @param from
     *            the first location
     * @param to
     *            the second location
     * @return the direction
     */
    public static Direction getDirection(final LocationData from, final LocationData to) {
        Direction direction = null;
        if ((from != null) && (to != null)) {
            // Calculate the length of the x and y components
            final int xComp = to.getX() - from.getX();
            final int yComp = to.getY() - from.getY();

            // Move in the longest distance first.
            if (Math.abs(xComp) >= Math.abs(yComp)) {
                if (xComp > 0) {
                    direction = Direction.EAST;
                } else if (xComp < 0) {
                    direction = Direction.WEST;
                }
            } else {
                if (yComp > 0) {
                    direction = Direction.NORTH;
                } else if (yComp < 0) {
                    direction = Direction.SOUTH;
                }
            }
        }
        return direction;
    }

    /**
     * Randomly choose one of the two return locations to hopefully cut down on
     * collisions
     * 
     * @return the LocationData to return gold to
     */
    public static LocationData getGoldReturnLocation() {
        if (RANDOM.nextBoolean()) {
            return RETURN_LOCATION_1;
        }
        return RETURN_LOCATION_2;
    }

    /**
     * Get the location in the given direction from the given tile
     * 
     * @param loc
     *            the given location
     * @param d
     *            the direction to look in
     * @return the location in the given direction
     */
    public static LocationData getLocationInDirection(final LocationData loc, final Direction d) {
        LocationData newLoc = null;
        switch (d) {
            case NORTH:
                newLoc = new LocationData(loc.getX(), loc.getY() + 1);
                break;
            case SOUTH:
                newLoc = new LocationData(loc.getX(), loc.getY() - 1);
                break;
            case EAST:
                newLoc = new LocationData(loc.getX() + 1, loc.getY());
                break;
            case WEST:
                newLoc = new LocationData(loc.getX() - 1, loc.getY());
                break;
            default:
                assert false;
                break;
        }
        return newLoc;
    }

    /**
     * Calculate the Manhattan distance between the two given locations
     * 
     * @param fx
     *            the first x-coordinate
     * @param fy
     *            the first y-coordinate
     * @param tx
     *            the second x-coordinate
     * @param ty
     *            the second y-coordinate
     * @return the Manhattan distance
     */
    public static int getManhattanDistance(final int fx, final int fy, final int tx, final int ty) {
        return Math.abs(fx - tx) + Math.abs(fy - ty);
    }

    /**
     * Calculate the Manhattan distance between the two given locations
     * 
     * @param from
     *            the first location
     * @param to
     *            the second location
     * @return the Manhattan distance
     */
    public static int getManhattanDistance(final LocationData from, final LocationData to) {
        return getManhattanDistance(from.getX(), from.getY(), to.getX(), to.getY());
    }

    /**
     * Get a random direction
     * 
     * @return the random direction
     */
    public static Direction getRandomDirection() {
        switch (RANDOM.nextInt(Direction.values().length)) {
            case 0:
                return Direction.NORTH;
            case 1:
                return Direction.EAST;
            case 2:
                return Direction.SOUTH;
            case 3:
                return Direction.WEST;
            default:
                assert false;
                return null;
        }
    }

    /**
     * Are the two given locations in a line?
     * 
     * @param from
     *            the first location
     * @param to
     *            the second location
     * @return true if the given locations are in a line
     */
    public static boolean inLine(final LocationData from, final LocationData to) {
        if ((from.getX() == to.getX()) || (from.getY() == to.getY())) {
            return true;
        }
        return false;
    }

    /**
     * Shared PRNG used for random operations in navigator
     */
    private static final Random RANDOM = new Random();

    /**
     * The first possible gold return location
     */
    private static final LocationData RETURN_LOCATION_1 = new LocationData(2, 3);

    /**
     * The second possible gold return location
     */
    private static final LocationData RETURN_LOCATION_2 = new LocationData(3, 2);
}
