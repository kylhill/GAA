package edu.ksu.cis.macr.simulator.capabilities.map;

import java.util.Comparator;

import edu.ksu.cis.macr.simulator.capabilities.LocationData;

/**
 * Compares one location with another to impose a total ordering.
 * 
 * @author Kyle Hill
 * 
 */
public final class LocationComparator implements Comparator<LocationData> {
    /**
     * Compares one location with another to impose a total ordering
     * 
     * @param loc
     *            the location to compare all other locations to
     */
    public LocationComparator(final LocationData loc) {
        location = loc;
    }

    @Override
    public int compare(final LocationData l1, final LocationData l2) {
        if (l1.equals(l2)) {
            return 0;
        }

        int f = MapUtils.getManhattanDistance(location, l1) - MapUtils.getManhattanDistance(location, l2);
        if (f == 0) {
            f = l1.getX() - l2.getX();
            if (f == 0) {
                f = l1.getY() - l2.getY();

                // If f == 0, then we should have found that l1.equals(l2). This
                // assert ensures that our comparator is consistent with equals.
                assert (f != 0);
            }
        }
        return f;
    }

    /**
     * The location to compare other locations to
     */
    private final LocationData location;
}
