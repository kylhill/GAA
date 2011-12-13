package edu.ksu.cis.macr.simulator.capabilities;

import java.util.Queue;

import edu.ksu.cis.macr.simulator.agent.Direction;
import edu.ksu.cis.macr.simulator.agent.IExecutionComponent;
import edu.ksu.cis.macr.simulator.agents.AbstractGaaAgent;
import edu.ksu.cis.macr.simulator.capabilities.map.MapUtils;
import edu.ksu.cis.macr.simulator.capabilities.map.Navigator;
import edu.ksu.cis.macr.simulator.capabilities.map.Tile;
import edu.ksu.cis.macr.simulator.capability.AbstractCapabilityAction;
import edu.ksu.cis.macr.simulator.capability.Failure;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.roles.interpreter.Name;

/**
 * The advanced Movement capability that allows navigation to arbitrary points
 * on the map
 * 
 * @author Kyle Hill
 * 
 */
public class AdvancedMovement extends AbstractCapabilityAction implements GaaCapability {
    /**
     * Constructs a new AdvancedMovement capability
     * 
     * @param ownerAgent
     *            the owning agent
     * @param environment
     *            the environment
     */
    public AdvancedMovement(final IExecutionComponent ownerAgent, final Environment environment) {
        super(AdvancedMovement.class, ownerAgent, environment);

        agent = (AbstractGaaAgent) ownerAgent;

        robotMovement = new RobotMovement(agent, environment);
        gps = new GPSImpl(agent, environment, getFailure());

        oldLocation = getCurrentLocation();
    }

    /**
     * Returns the current location of this agent
     * 
     * @return the current location of this agent
     */
    public final LocationData getCurrentLocation() {
        return gps.read();
    }

    /**
     * Get the agent's destination location
     * 
     * @return the agent's destination location
     */
    public final LocationData getDestinationLocation() {
        return destinationLocation;
    }

    @Override
    public final double getFailure() {
        return Failure.MIN_FAILURE;
    }

    /**
     * Returns the previous location of this agent
     * 
     * @return the previous location of this agent
     */
    public final LocationData getOldLocation() {
        return oldLocation;
    }

    /**
     * Returns true if the current location is the same as the destination
     * location
     * 
     * @return true if the current location is the same as the destination
     *         location
     */
    public final boolean isAtDestinationLocation() {
        return getCurrentLocation().equals(getDestinationLocation());
    }

    /**
     * Move this agent toward its current destination.
     * 
     * @return true if this agent actually moved, false otherwise
     */
    public final boolean move() {
        boolean moved = false;

        // Get the next movement towards our destination
        final Direction d = getNextMovement();
        if (d != null) {
            final LocationData curLoc = getCurrentLocation();

            // Actually move the robot
            moved = robotMovement.move(d);
            if (moved) {
                oldLocation = curLoc;
            } else if (!agent.getMap().isSurrounded(getCurrentLocation())) {
                // If we couldn't move, then we need to handle the collision
                moved = handleCollision();
            }
        }

        if (!moved) {
            // We can't move, end our turn in hopes of someone moving out of
            // the way.
            endTurn();
        }
        return moved;
    }

    @Override
    public final void reset() {
        robotMovement.reset();
        gps.reset();
    }

    /**
     * Set this agent's destination location
     * 
     * @param loc
     *            the new destination location
     */
    public final void setDestinationLocation(@Name("loc") final LocationData loc) {
        if (!loc.equals(destinationLocation)) {
            destinationLocation = loc;
            collisionCount = 0;
        }
    }

    /**
     * Get the next movement this agent should perform in pursuit of its current
     * destination
     * 
     * @return the direction this agent should move next
     */
    private final Direction getNextMovement() {
        final LocationData destination = getDestinationLocation();
        if (destination != null) {
            // Find a path to our destination location
            final Queue<LocationData> path = navigator.findPath(agent.getMap(), getCurrentLocation(), destination);
            if (path == null) {
                // We cannot get to our destination if we get a null path
                return null;

            } else if (path.isEmpty()) {
                // We've reached our destination if we get an empty path. We
                // shouldn't be trying to move since we're already at
                // our destination!
                assert false;
                return null;
            }

            // Move towards our destination location
            return MapUtils.getDirection(getCurrentLocation(), path.peek());
        }

        // We have no destination location set
        assert false;
        return null;
    }

    /**
     * Handle collisions with unknown obstacles (other agents) by moving in a
     * random direction temporarily, and then recalculating our route
     * 
     * @return true if the agent eventually moved, false otherwise
     */
    private final boolean handleCollision() {
        final int LOOP_THRESHOLD = 25;

        // If we didn't actually move to the new location, handle the collision
        boolean hasMoved = false;
        while (!hasMoved && (collisionCount < LOOP_THRESHOLD)) {
            collisionCount++;

            // Attempt to move in a new random direction until we've actually
            // moved
            final Direction d = MapUtils.getRandomDirection();

            final LocationData curLoc = getCurrentLocation();
            final Tile nextTile = agent.getMap().getTile(curLoc, d);
            if (!nextTile.hasObstruction() && !nextTile.isDangerous()) {
                hasMoved = robotMovement.move(d);
                if (hasMoved) {
                    oldLocation = curLoc;
                }
            }
        }

        // Our collision handling code should really be able to take care of
        // moving in LOOP_THRESHOLD attempts. However, sometimes we cannot reach
        // the location we wish to if an agent decides to end its activity at
        // our goal location.
        return hasMoved;
    }

    /**
     * The agent who owns this capability
     */
    private final AbstractGaaAgent agent;

    /**
     * The number of times we've collided with an obstacle
     */
    private int collisionCount = 0;

    /**
     * This capability's current destination
     */
    private LocationData destinationLocation = null;

    /**
     * The underlying GPS capability;
     */
    private final GPSImpl gps;

    /**
     * The navigator used by this capability
     */
    private final Navigator navigator = new Navigator();

    /**
     * This capability's agent's previous destination
     */
    private LocationData oldLocation = null;

    /**
     * The underlying movement capability
     */
    private final RobotMovement robotMovement;
}
