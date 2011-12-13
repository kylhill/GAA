package edu.ksu.cis.macr.simulator.capabilities.map;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import edu.ksu.cis.macr.simulator.agent.Direction;
import edu.ksu.cis.macr.simulator.capabilities.LocationData;

/**
 * Map of Wumpi World as the agent sees it
 * 
 * @author Kyle Hill
 */
public class Map implements Serializable {
    /**
     * The type of range: Square or Manhattan
     * 
     * @author Kyle Hill
     * 
     */
    public enum RangeType {
        /**
         * Manhattan distance
         */
        MANHATTAN,

        /**
         * Square distance
         */
        SQUARE
    }

    /**
     * Map Constructor
     */
    public Map() {
        for (int x = 1; x < MAX_X; x++) {
            for (int y = 1; y < MAX_Y; y++) {
                map[x][y] = new Tile(this, new LocationData(x, y));
            }
        }
    }

    /**
     * Find all known gold locations on the map.
     * 
     * @param from
     *            the location to sort locaitons from
     * @return a queue of gold locations sorted by distance from the given
     *         location
     */
    public final Queue<LocationData> findGold(final LocationData from) {
        final LinkedList<LocationData> locations = new LinkedList<LocationData>();

        // Search the entire map to see if any tiles contain glitter
        for (int x = 1; x < Map.MAX_X; x++) {
            for (int y = 1; y < Map.MAX_Y; y++) {
                final Tile tile = getTile(x, y);
                if (!tile.isClaimed() && !tile.isDangerous() && tile.hasGlitter()) {
                    locations.add(tile.getLocation());
                }
            }
        }
        Collections.sort(locations, new LocationComparator(from));
        return locations;
    }

    /**
     * Get the nearest unsearched tile to the given location
     * 
     * @param from
     *            the location to search from
     * @return the nearest unsearched location, null if the entire map has been
     *         searched
     */
    public final LocationData findNearestUnsearchedLocation(final LocationData from) {
        // Search the map for all unsearched locations
        final LinkedList<LocationData> locations = new LinkedList<LocationData>();
        for (int x = 1; x < MAX_X; x++) {
            for (int y = 1; y < MAX_Y; y++) {
                final Tile tile = getTile(x, y);
                if (!tile.hasSearched() && !tile.hasObstruction() && !tile.isDangerous()) {
                    locations.add(tile.getLocation());
                }
            }
        }

        // Find the closest location to our current location
        LocationData minLoc = null;
        if (!locations.isEmpty()) {
            final LocationComparator comparator = new LocationComparator(from);
            for (final LocationData loc : locations) {
                if (minLoc == null) {
                    minLoc = loc;
                } else if (comparator.compare(minLoc, loc) > 0) {
                    minLoc = loc;
                }
            }
        }
        return minLoc;
    }

    /**
     * Find all known Wumpi locations on the map.
     * 
     * @param from
     *            the location to search from
     * @return a queue of Wumpi locations sorted by distance from the agent's
     *         current location
     */
    public final Queue<LocationData> findWumpi(final LocationData from) {
        final LinkedList<LocationData> locations = new LinkedList<LocationData>();

        // Search the entire map to see if any tiles contain a Wumpi
        for (int x = 1; x < Map.MAX_X; x++) {
            for (int y = 1; y < Map.MAX_Y; y++) {
                final Tile tile = getTile(x, y);
                if (!tile.isClaimed() && tile.isWumpi()) {
                    locations.add(tile.getLocation());
                }
            }
        }
        Collections.sort(locations, new LocationComparator(from));
        return locations;
    }

    /**
     * Get neighboring tiles satisfying the given parameters
     * 
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     * @param range
     *            range to search
     * @param t
     *            the type of range to search
     * @return the collection of tiles
     */
    public final Collection<Tile> getNeighbors(final int x, final int y, final int range, final RangeType t) {
        return getNeighbors(x, y, range, t, -range, range, -range, range);
    }

    /**
     * Get neighboring tiles satisfying the given parameters
     * 
     * @param loc
     *            the location to search from
     * @param range
     *            range to search
     * @param t
     *            the type of range to search
     * @return the collection of tiles
     */
    public final Collection<Tile> getNeighbors(final LocationData loc, final int range, final RangeType t) {
        return getNeighbors(loc.getX(), loc.getY(), range, t, -range, range, -range, range);
    }

    /**
     * Get the tile at the given location
     * 
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     * @return the tile
     */
    public final Tile getTile(final int x, final int y) {
        assert ((x > 0) && (y > 0));
        assert ((x < MAX_X) && (y < MAX_Y));

        return map[x][y];
    }

    /**
     * Get the tile at the given location
     * 
     * @param loc
     *            the location
     * @return the tile
     */
    public final Tile getTile(final LocationData loc) {
        return getTile(loc.getX(), loc.getY());
    }

    /**
     * Get the tile in the given direction from the given location
     * 
     * @param loc
     *            the location
     * @param dir
     *            the direction
     * @return the tile
     */
    public final Tile getTile(final LocationData loc, final Direction dir) {
        switch (dir) {
            case NORTH:
                return getTile(loc.getX(), loc.getY() + 1);
            case SOUTH:
                return getTile(loc.getX(), loc.getY() - 1);
            case EAST:
                return getTile(loc.getX() + 1, loc.getY());
            case WEST:
                return getTile(loc.getX() - 1, loc.getY());
        }

        assert false;
        return null;
    }

    /**
     * Get the collection of tiles between the two given tiles. No tiles are
     * returned if the two given tiles are not in line with each other.
     * 
     * @param from
     *            the tile to search from
     * @param to
     *            the tile to search to
     * @return the collection of tiles
     */
    public final Collection<Tile> getTilesBetween(final LocationData from, final LocationData to) {
        final Collection<Tile> betweenList = new ArrayList<Tile>();
        if (!from.equals(to) && MapUtils.inLine(from, to)) {
            final Direction d = MapUtils.getDirection(from, to);

            Tile next = getTile(from);
            final Tile target = getTile(to);
            do {
                next = getTile(next.getLocation(), d);
                if (!next.equals(target)) {
                    betweenList.add(next);
                }
            } while (!next.equals(target));
        }
        return betweenList;
    }

    /**
     * Is there an obstruction between the two given locations?
     * 
     * @param from
     *            the first location
     * @param to
     *            the second location
     * @return true if there is an obstruction between the given locations,
     *         false otherwise
     */
    public final boolean isObstructionBetween(final LocationData from, final LocationData to) {
        for (final Tile tile : getTilesBetween(from, to)) {
            if (tile.hasObstruction()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is the given location surrounded by obstacles, or danger?
     * 
     * @param loc
     *            the location
     * 
     * @return true if the given location is surronded by obstacles or danger
     */
    public final boolean isSurrounded(final LocationData loc) {
        for (final Tile tile : getNeighbors(loc, 1, Map.RangeType.MANHATTAN)) {
            if (!tile.hasObstruction() && !tile.isDangerous()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Merge this map's information with the given map
     * 
     * @param other
     *            the other map
     */
    public final void mergeFrom(final Map other) {
        for (int x = 1; x < MAX_X; x++) {
            for (int y = 1; y < MAX_Y; y++) {
                getTile(x, y).mergeFrom(other.getTile(x, y));
            }
        }
    }

    /**
     * Update breeze information on the map
     * 
     * @param oldLoc
     *            old agent location
     * @param newLoc
     *            new agent location
     * @param hasBreeze
     *            if the agent detected a breeze in newLoc
     * @return if any tiles were updated
     */
    public final boolean updateBreeze(final LocationData oldLoc, final LocationData newLoc, final boolean hasBreeze) {
        boolean updated = false;

        if (!newLoc.equals(oldLoc)) {
            // Get the smallest set of neighboring tiles that could contain a
            // pit
            Collection<Tile> neighbors;
            if ((oldLoc == null) || !hasBreeze) {
                neighbors = getNeighbors(newLoc, BREEZE_RANGE, BREEZE_RANGE_TYPE);
            } else {
                final Direction d = MapUtils.getDirection(oldLoc, newLoc);
                neighbors = getNeighborsInDirection(newLoc, d, BREEZE_RANGE, BREEZE_RANGE_TYPE);
            }

            for (final Tile tile : neighbors) {
                updated |= tile.setBreeze(hasBreeze);
            }
        }
        return updated;
    }

    /**
     * Update glitter information on the map
     * 
     * @param oldLoc
     *            old agent location
     * @param newLoc
     *            new agent location
     * @param hasGlitter
     *            if the agent detected glitter in newLoc
     * @return if any tiles were updated
     */
    public final boolean updateGlitter(final LocationData oldLoc, final LocationData newLoc, final boolean hasGlitter) {
        boolean updated = false;

        if (!newLoc.equals(oldLoc)) {
            // Get the smallest set of neighboring tiles that could contain gold
            Collection<Tile> neighbors;
            if ((oldLoc == null) || !hasGlitter) {
                neighbors = getNeighbors(newLoc, GLITTER_RANGE, GLITTER_RANGE_TYPE);
            } else {
                neighbors = new ArrayList<Tile>(3);

                final Direction d = MapUtils.getDirection(oldLoc, newLoc);
                neighbors.add(getTile(newLoc, d));
                neighbors.add(getTile(newLoc, d.turnLeft()));
                neighbors.add(getTile(newLoc, d.turnRight()));
            }

            // Set the glitter flag on our neighbors
            for (final Tile tile : neighbors) {
                updated |= tile.setGlitter(hasGlitter);
            }
        }
        return updated;
    }

    /**
     * Update obstruction information on the map
     * 
     * @param loc
     *            new agent location
     * @param sonarData
     *            the collection of sonar data
     * @return if any tiles were updated
     */
    public final boolean updateObstructions(final LocationData loc, final Collection<LocationData> sonarData) {
        boolean updated = false;

        // Set those tiles in which we discovered obstructions
        for (final LocationData relLoc : sonarData) {
            final LocationData absLoc = new LocationData(loc.getX() + relLoc.getX(), loc.getY() + relLoc.getY());
            if ((absLoc.getX() >= 1) && (absLoc.getX() < Map.MAX_X) && (absLoc.getY() >= 1) && (absLoc.getY() < Map.MAX_Y)) {
                updated |= getTile(absLoc).setObstruction(true);
            }
        }
        return updated;
    }

    /**
     * Update searched information on the map
     * 
     * @param loc
     *            the new agent location
     * @return if any tiles were updated
     */
    public final boolean updateSearched(final LocationData loc) {
        boolean updated = getTile(loc).setSearched(true);
        for (final Tile tile : getNeighbors(loc, GLITTER_RANGE, GLITTER_RANGE_TYPE)) {
            updated |= tile.setSearched(true);
        }
        return updated;
    }

    /**
     * Update smell information on the map
     * 
     * @param oldLoc
     *            old agent location
     * @param newLoc
     *            new agent location
     * @param hasSmell
     *            if the agent detected smell in newLoc
     * @return if any tiles were updated
     */
    public final boolean updateSmell(final LocationData oldLoc, final LocationData newLoc, final boolean hasSmell) {
        boolean updated = false;

        if (hasSmell) {
            if (!newLoc.equals(oldLoc)) {
                // Get the smallest set of neighboring tiles that could contain
                // a wumpi
                Collection<Tile> neighbors;
                if (oldLoc == null) {
                    neighbors = getNeighbors(newLoc, SMELL_RANGE, SMELL_RANGE_TYPE);
                } else {
                    final Direction d = MapUtils.getDirection(oldLoc, newLoc);
                    neighbors = getNeighborsInDirection(newLoc, d, SMELL_RANGE, SMELL_RANGE_TYPE);
                }

                // Only update tiles to have a smell if we can also detect an
                // obstruction that isn't a wall within range
                for (final Tile tile : neighbors) {
                    if (tile.hasObstruction() && !tile.isWall()) {
                        for (final Tile smellCandidate : getNeighbors(tile.getLocation(), SMELL_RANGE - 1, SMELL_RANGE_TYPE)) {
                            if (neighbors.contains(smellCandidate)) {
                                updated |= smellCandidate.setSmell(true);
                            }
                        }
                    }
                }
            }
        } else {
            // If we don't smell a Wumpi at our current location, clear the
            // smell flag on all tiles within the range we entered
            for (final Tile m : getNeighbors(newLoc, SMELL_RANGE - 1, SMELL_RANGE_TYPE)) {
                updated |= m.setSmell(false);
            }
        }
        return updated;
    }

    /**
     * Update the given location to be visited
     * 
     * @param loc
     *            the agent location
     * @return if the tile was updated
     */
    public final boolean updateVisited(final LocationData loc) {
        return getTile(loc).setVisited(true);
    }

    /**
     * Get the collection of neighboring tiles that satisfy the given parameters
     * 
     * @param sX
     *            the source x-coordinate
     * @param sY
     *            the source y-coordinate
     * @param range
     *            the range to search
     * @param t
     *            the type of range to search
     * @param iX
     *            the initial x-coordinate
     * @param nX
     *            the number of tiles on the x axis
     * @param iY
     *            the initial y-coordinate
     * @param nY
     *            the number of tiles on the y axis
     * @return the collection of tiles
     */
    private final Collection<Tile> getNeighbors(final int sX, final int sY, final int range, final RangeType t, final int iX, final int nX, final int iY, final int nY) {
        final Collection<Tile> neighbors = new ArrayList<Tile>();
        if (range > 0) {
            for (int x = iX; x <= nX; x++) {
                for (int y = iY; y <= nY; y++) {
                    if ((x == 0) && (y == 0)) {
                        // Don't add the current tile
                    } else {
                        if ((t == RangeType.SQUARE) || ((t == RangeType.MANHATTAN) && ((Math.abs(x) + Math.abs(y)) <= range))) {
                            final int rX = sX + x;
                            final int rY = sY + y;
                            if ((rX >= 1) && (rX < MAX_X) && (rY >= 1) && (rY < MAX_Y)) {
                                neighbors.add(getTile(rX, rY));
                            }
                        }
                    }
                }
            }
        }
        return neighbors;
    }

    /**
     * Get neighboring tiles satisfying the given parameters
     * 
     * @param loc
     *            the location to search from
     * @param dir
     *            the direction to search in
     * @param range
     *            range to search
     * @param t
     *            the type of range to search
     * @return the collection of tiles
     */
    private final Collection<Tile> getNeighborsInDirection(final LocationData loc, final Direction dir, final int range, final RangeType t) {
        if (dir == null) {
            return getNeighbors(loc, range, t);
        }

        int iX, iY, nX, nY;
        switch (dir) {
            case NORTH:
                iX = -range;
                nX = range;
                iY = 1;
                nY = range;
                break;
            case SOUTH:
                iX = -range;
                nX = range;
                iY = -range;
                nY = -1;
                break;
            case EAST:
                iX = 1;
                nX = range;
                iY = -range;
                nY = range;
                break;
            case WEST:
                iX = -range;
                nX = -1;
                iY = -range;
                nY = range;
                break;
            default:
                assert false;
                iX = 0;
                nX = 0;
                iY = 0;
                nY = 0;
                break;
        }
        return getNeighbors(loc.getX(), loc.getY(), range, t, iX, nX, iY, nY);
    }

    /**
     * The range at which breezes can be detected
     */
    public static final int BREEZE_RANGE = 1;

    /**
     * The type of range for breezes
     */
    public static final RangeType BREEZE_RANGE_TYPE = RangeType.SQUARE;

    /**
     * The range at which glitter can be detected
     */
    public static final int GLITTER_RANGE = 1;

    /**
     * The type of range for glitter
     */
    public static final RangeType GLITTER_RANGE_TYPE = RangeType.MANHATTAN;

    /**
     * The maximum size for the world's X coordinate
     */
    public static final int MAX_X = 47;

    /**
     * The maximum size for the world's Y coordinate
     */
    public static final int MAX_Y = 26;

    /**
     * The range at which smells can be detected
     */
    public static final int SMELL_RANGE = 2;

    /**
     * The type of range for smells
     */
    public static final RangeType SMELL_RANGE_TYPE = RangeType.SQUARE;

    /**
     * The range at which obstacles can be detected
     */
    public static final int SONAR_RANGE = 5;

    /**
     * The type of range for obstacles
     */
    public static final RangeType SONAR_RANGE_TYPE = RangeType.MANHATTAN;

    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The tile array that represents this map
     */
    private final Tile[][] map = new Tile[MAX_X][MAX_Y];
}
