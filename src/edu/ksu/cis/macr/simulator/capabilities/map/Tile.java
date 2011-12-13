package edu.ksu.cis.macr.simulator.capabilities.map;

import java.io.Serializable;

import edu.ksu.cis.macr.simulator.capabilities.LocationData;

/**
 * Represents the state of a tile in Wumpi World
 * 
 * @author Kyle Hill
 */
public class Tile implements Serializable {
    /**
     * Initializes a tile in the given map at the given location
     * 
     * @param m
     *            the map
     * @param loc
     *            the location
     */
    public Tile(final Map m, final LocationData loc) {
        map = m;
        location = loc;
    }

    @Override
    public final boolean equals(final Object o) {
        if (!(o instanceof Tile)) {
            return false;
        }
        final Tile other = (Tile) o;
        return location.equals(other.location);
    }

    /**
     * Get this tile's location
     * 
     * @return the tile's location
     */
    public final LocationData getLocation() {
        return location;
    }

    /**
     * Does this tile have a breeze?
     * 
     * @return true if this tile has a breeze, false otherwise
     */
    public final boolean hasBreeze() {
        // Tiles that we have visited or have an obstruction cannot contain a
        // breeze
        return hasBreeze && !hasObstruction() && !hasVisited();
    }

    /**
     * Does this tile have glitter?
     * 
     * @return true if this tile has glitter, false otherwise
     */
    public final boolean hasGlitter() {
        // Tiles that contain an obstruction cannot contain gold
        return hasGlitter && !hasObstruction();
    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }

    /**
     * Does this tile have an obstruction? Obstructions can either be a wall,
     * Wumpi, or another agent.
     * 
     * @return true if this tile has an obstruction, false otherwise
     */
    public final boolean hasObstruction() {
        // Tiles that we have visited cannot contain an obstruction
        return hasObstruction && !hasVisited();
    }

    /**
     * Has this tile been searched for gold? (among other things)
     * 
     * @return true if this tile has been searched, false otherwise
     */
    public final boolean hasSearched() {
        return hasSearched;
    }

    /**
     * Does this tile contain a smell?
     * 
     * @return true if this tile has a smell, false otherwise
     */
    public final boolean hasSmell() {
        // Tiles that contain obstructions or that we have visited cannot
        // contain a smell
        return hasSmell && !hasObstruction() && !hasVisited();
    }

    /**
     * Has this tile been visited by an agent?
     * 
     * @return true if it has been visited by an agent, false otherwise
     */
    public final boolean hasVisited() {
        return hasVisited;
    }

    /**
     * Returns true if this tile has been "claimed" by an agent, false otherwise
     * 
     * @return true if this tile has been "claimed" by an agent, false otherwise
     */
    public final boolean isClaimed() {
        return isClaimed && (hasGlitter() || isWumpi());
    }

    /**
     * Is it dangerous to move into this tile?
     * 
     * @return true if moving into this tile could result in death, false
     *         otherwise
     */
    public final boolean isDangerous() {
        // We consider a tile dangerous if it could possibly contain a pit or
        // Wumpi.
        return hasSmell() || hasBreeze();
    }

    /**
     * Is this tile a wall?
     * 
     * @return true if this tile is a wall, false otherwise
     */
    public final boolean isWall() {
        // We know a tile is a wall if we've searched it or visited any of its
        // direct neighbors and it has an obstruction
        if (hasObstruction()) {
            if (hasSearched()) {
                return true;
            }

            for (final Tile tile : map.getNeighbors(getLocation(), Map.SMELL_RANGE - 1, Map.SMELL_RANGE_TYPE)) {
                if (tile.hasVisited()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Based on our best guess, is this tile a Wumpi? This can sometimes return
     * false positives based on incomplete sensor data.
     * 
     * @return true if we think this tile is a Wumpi, false otherwise
     */
    public final boolean isWumpi() {
        // This tile can't have a Wumpi if it doesn't have an obstruction, or if
        // we already know that this location is a wall.
        if (!hasObstruction() || isWall()) {
            return false;
        }

        // See if there are any other obstructions adjacent to this tile. If
        // it's surrounded by too many, it's probably a wall
        final int WALL_THRESHOLD = 2;

        int obsCount = 0;
        for (final Tile tile : map.getNeighbors(getLocation(), 1, Map.RangeType.MANHATTAN)) {
            if (tile.hasObstruction()) {
                obsCount++;
                if (obsCount >= WALL_THRESHOLD) {
                    return false;
                }
            }
        }

        // See if this tile is surrounded by enough smelly tiles to have a good
        // chance of being a Wumpi
        final int WUMPI_THRESHOLD = 4;

        int smellCount = 0;
        for (final Tile tile : map.getNeighbors(getLocation(), Map.SMELL_RANGE - 1, Map.SMELL_RANGE_TYPE)) {
            if (tile.hasSmell()) {
                smellCount++;
                if (smellCount >= WUMPI_THRESHOLD) {
                    break;
                }
            }
        }
        return smellCount >= WUMPI_THRESHOLD;
    }

    /**
     * Combine data from the given tile with what we know about this tile
     * 
     * @param other
     *            the other tile
     */
    public final void mergeFrom(final Tile other) {
        // Only attempt to merge tiles at the same location
        if (location.equals(other.location)) {
            if (!hasSearched() && other.hasSearched()) {
                // Trust the other tile since it has been searched and this
                // one has not
                hasBreeze = other.hasBreeze;
                hasGlitter = other.hasGlitter;
                hasObstruction = other.hasObstruction;
                hasSearched = other.hasSearched;
                hasSmell = other.hasSmell;
                isClaimed = other.isClaimed;

                // Don't let someone else tell us that we have not visited a
                // place if we already have
                setVisited(other.hasVisited());
            } else {
                // Use public setters to sanitize the data from the other tile.
                // Only allow false positives, not false negatives.
                if (other.hasVisited()) {
                    setVisited(true);
                }
                if (other.hasSearched()) {
                    setSearched(true);
                }
                if (other.hasObstruction()) {
                    setObstruction(true);
                }
                if (other.isClaimed()) {
                    setClaimed();
                }
            }
        } else {
            assert false;
        }
    }

    /**
     * Set this tile to have a breeze. This will only update to true if it is
     * possible for this tile to have a breeze
     * 
     * @param b
     *            does this tile have a breeze?
     * @return if this tile's state has changed
     */
    public final boolean setBreeze(final boolean b) {
        final boolean old = hasBreeze;

        // Don't allow tiles with obstructions, tiles we've searched or visited
        // to have the breeze flag set to true again
        if (!hasObstruction() && !(b && hasSearched()) && !(b && hasVisited())) {
            hasBreeze = b;
        }
        return old != hasBreeze;
    }

    /**
     * Mark this tile as "claimed" so other agents don't try to take action
     * against it
     */
    public final void setClaimed() {
        isClaimed = (hasGlitter() || isWumpi());
    }

    /**
     * Set this tile to have a glitter. This will only update to true if it is
     * possible for this tile to have a glitter.
     * 
     * @param g
     *            does this tile have a glitter?
     * @return if this tile's state has changed
     */
    public final boolean setGlitter(final boolean g) {
        final boolean old = hasGlitter;

        // Don't allow tiles with obstructions, or tiles that we've searched to
        // have the glitter flag set to true again
        if (!hasObstruction() && !(g && hasSearched())) {
            hasGlitter = g;
        }
        return old != hasGlitter;
    }

    /**
     * Set this tile to have an obstruction. This will only update to true if it
     * is possible for this tile to have an obstruction.
     * 
     * @param o
     *            does this tile have an obstruction?
     * @return if this tile's state has changed
     */
    public final boolean setObstruction(final boolean o) {
        final boolean old = hasObstruction;

        // Don't allow tiles that we've searched or visited to have the
        // obstruction flag set to true again
        if (!(o && hasSearched()) && !(o && hasVisited())) {
            hasObstruction = o;
        }
        return old != hasObstruction;
    }

    /**
     * Set the searched flag for this tile
     * 
     * @param s
     *            have we searched this tile?
     * @return if this tile's state has changed
     */
    public final boolean setSearched(final boolean s) {
        final boolean old = hasSearched;
        hasSearched = s;
        return old != hasSearched;
    }

    /**
     * Set this tile to have a smell. This will only update to true if it is
     * possible for this tile to have a smell.
     * 
     * @param s
     *            does this tile have a smell?
     * @return if this tile's state has changed
     */
    public final boolean setSmell(final boolean s) {
        final boolean old = hasSmell;

        // Don't allow tiles with obstructions, tiles we've searched or visited
        // to have the smell flag set to true again
        if (!hasObstruction() && !(s && hasSearched()) && !(s && hasVisited())) {
            hasSmell = s;

            // When clearing the smell flag, make sure to mark this tile as
            // unsearched so agents try to check it again
            if (old && !hasSmell) {
                setSearched(false);
            }
        }
        return old != hasSmell;
    }

    /**
     * Set the visited flag for this tile
     * 
     * @param v
     *            have we visited this tile?
     * @return if this tile's state has changed
     */
    public final boolean setVisited(final boolean v) {
        final boolean old = hasVisited;

        // We can't ever "unvisit" a place
        if (!hasVisited && v) {
            hasVisited = v;
        }
        return old != hasVisited;
    }

    /**
     * Get the cost of moving through this tile.
     * 
     * @return the cost of moving through this tile
     */
    public static final int getCost() {
        return COST;
    }

    /**
     * The cost of moving through a tile
     */
    public static final int COST = 1;

    /**
     * The serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * This tile's breeze flag
     */
    private boolean hasBreeze = false;

    /**
     * This tile's glitter flag
     */
    private boolean hasGlitter = false;

    /**
     * This tile's obstruction flag
     */
    private boolean hasObstruction = false;

    /**
     * This tile's searched flag
     */
    private boolean hasSearched = false;

    /**
     * This tile's smell flag
     */
    private boolean hasSmell = false;

    /**
     * This tile's visited flag
     */
    private boolean hasVisited = false;

    /**
     * true if this tile has been "claimed" by us, or another agent
     */
    private boolean isClaimed = false;

    /**
     * This tile's location
     */
    private final LocationData location;

    /**
     * The map this tile belongs to
     */
    private final Map map;
}
