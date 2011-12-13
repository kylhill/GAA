package edu.ksu.cis.macr.simulator.capabilities.map;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.TreeSet;

import edu.ksu.cis.macr.simulator.capabilities.LocationData;

/**
 * Collection of navigation utilities and search functions
 * 
 * @author Kyle Hill
 */
public class Navigator {
    /**
     * Internal search node representation for findPath()
     * 
     * @author Kyle Hill
     */
    private static final class Node implements Comparable<Node> {
        /**
         * Node constructor
         * 
         * @param aX
         *            x-coordinate
         * @param aY
         *            y-coordinate
         */
        public Node(final int aX, final int aY) {
            x = aX;
            y = aY;
        }

        @Override
        public int compareTo(final Node other) {
            // Two Nodes are equal if and only if they have the same x and y
            // coordinates
            if (this == other || ((x == other.x) && (y == other.y))) {
                return 0;
            }

            // Compare two nodes based on heuristic + cost, then position
            int f = (heuristic + cost) - (other.heuristic + other.cost);
            if (f == 0) {
                f = x - other.x;
                if (f == 0) {
                    f = y - other.y;
                }
            }

            assert f != 0;
            return f;
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Node)) {
                return false;
            }

            // We guarantee that we are compatible with equals by implementing
            // equals as a call to compareTo
            return compareTo((Node) o) == 0;
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = (37 * result) + x;
            result = (37 * result) + y;

            return result;
        }

        /**
         * Resets this node back to its default properties
         */
        public void reset() {
            cost = Tile.COST;
            heuristic = 0;
            parent = null;
        }

        /**
         * The cost of traveling across this node
         */
        int cost = Tile.COST;

        /**
         * The guess as to how expensive getting to this node will be
         */
        int heuristic = 0;

        /**
         * The parent of this node
         */
        Node parent = null;

        /**
         * This node's x position
         */
        final int x;

        /**
         * This node's y position
         */
        final int y;
    }

    /**
     * Navigator Constructor
     */
    public Navigator() {
        nodes = new Node[Map.MAX_X][Map.MAX_Y];

        for (int x = 0; x < Map.MAX_X; x++) {
            for (int y = 0; y < Map.MAX_Y; y++) {
                nodes[x][y] = new Node(x, y);
            }
        }
    }

    /**
     * A simple implementation of the A* path finding algorithm.
     * 
     * @param map
     *            the map to search within
     * @param from
     *            the location to start from
     * @param to
     *            the location to get to
     * @return the path to follow to get from from to to, null if no path can be
     *         made, empty if at the destination already
     */
    public final Queue<LocationData> findPath(final Map map, final LocationData from, final LocationData to) {
        // If we know we can't reach our goal, exit immediately
        final Tile t = map.getTile(to);
        if (t.hasObstruction() || t.isDangerous()) {
            return null;
        }

        final Node source = nodes[from.getX()][from.getY()];
        Node goal = nodes[to.getX()][to.getY()];

        // If we're at our goal already, no need to calculate a path
        if (source == goal) {
            return new LinkedList<LocationData>();
        }

        // Reset all nodes back to their default values
        resetNodes();

        final TreeSet<Node> open = new TreeSet<Node>();
        final TreeSet<Node> closed = new TreeSet<Node>();

        open.add(source);

        // While we haven't explored all possible paths to the goal
        while (!open.isEmpty()) {
            // Get the first node in our open list. This is most likely to be
            // the next step in the path.
            final Node cur = open.pollFirst();
            if (cur == goal) {
                // We've found our goal, we're done
                break;
            }

            // Add current node to the closed list
            closed.add(cur);

            // Search all of our immediate neighbors to find the next best step
            final Collection<Tile> neighbors = map.getNeighbors(cur.x, cur.y, 1, Map.RangeType.MANHATTAN);
            for (final Tile tile : neighbors) {
                if (!tile.hasObstruction() && !tile.isDangerous()) {
                    final Node neighbor = nodes[tile.getLocation().getX()][tile.getLocation().getY()];

                    // If we find the cost to get to this neighbor is lower than
                    // we thought it was previously, make sure it gets
                    // re-evaluated in case we've found a better path
                    final int nextCost = cur.cost + Tile.getCost();
                    if (nextCost < neighbor.cost) {
                        open.remove(neighbor);
                        closed.remove(neighbor);
                    }

                    // If we haven't seen this neighbor yet, update its
                    // properties and add it to the open list
                    if (!open.contains(neighbor) && !closed.contains(neighbor)) {
                        neighbor.cost = nextCost;
                        neighbor.heuristic = MapUtils.getManhattanDistance(tile.getLocation().getX(), tile.getLocation().getY(), to.getX(), to.getY());
                        neighbor.parent = cur;
                        open.add(neighbor);
                    }
                }
            }
        }

        // If we didn't make it to our destination, return an empty path
        if (goal.parent == null) {
            return null;
        }

        // Construct path to goal by following parent pointers back to the start
        final LinkedList<LocationData> path = new LinkedList<LocationData>();
        while (goal != source) {
            final LocationData loc = new LocationData(goal.x, goal.y);
            path.addFirst(loc);
            goal = goal.parent;
        }

        assert path.getLast().equals(to);
        return path;
    }

    /**
     * Resets all nodes in this Navigator back to their default
     */
    private final void resetNodes() {
        for (int x = 0; x < Map.MAX_X; x++) {
            for (int y = 0; y < Map.MAX_Y; y++) {
                nodes[x][y].reset();
            }
        }
    }

    /**
     * The set of nodes that make up our map
     */
    private final Node[][] nodes;
}
