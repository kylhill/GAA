package edu.ksu.cis.macr.simulator.capabilities;

import java.util.Collections;
import java.util.LinkedList;

import edu.ksu.cis.macr.simulator.agent.IExecutionComponent;
import edu.ksu.cis.macr.simulator.agents.AbstractGaaAgent;
import edu.ksu.cis.macr.simulator.capabilities.map.LocationComparator;
import edu.ksu.cis.macr.simulator.capabilities.map.Map;
import edu.ksu.cis.macr.simulator.capabilities.map.Map.RangeType;
import edu.ksu.cis.macr.simulator.capabilities.map.MapUtils;
import edu.ksu.cis.macr.simulator.capabilities.map.Tile;
import edu.ksu.cis.macr.simulator.capability.AbstractCapabilityAction;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.roles.interpreter.Name;

/**
 * The AdvancedBazooka capability wraps a Bazooka capability internally
 * 
 * @author Kyle Hill
 * 
 */
public class AdvancedBazooka extends AbstractCapabilityAction implements GaaCapability {
    /**
     * Constructs a new AdvancedBazooka
     * 
     * @param ownerAgent
     *            the agent who owns this capability
     * @param environment
     *            the environment this capability exists within
     */
    public AdvancedBazooka(final IExecutionComponent ownerAgent, final Environment environment) {
        super(AdvancedBazooka.class, ownerAgent, environment);

        agent = (AbstractGaaAgent) ownerAgent;

        bazooka = new Bazooka(ownerAgent, environment);
    }

    /**
     * Attempts to fire at the given target location
     * 
     * @param loc
     *            the location to fire upon
     * 
     * @return true if the bazooka was fired, false otherwise
     */
    public final boolean fire(@Name("loc") final LocationData loc) {
        boolean wumpiKilled = false;

        if (canFire(loc)) {
            // Fire, assume we kill the wumpi
            bazooka.fire(MapUtils.getDirection(agent.getMover().getCurrentLocation(), loc));

            // Clear the smell flag on all tiles within the kill radius.
            // NOTE: This assumes no wumpi are ever adjacent to each other!
            for (final Tile tile : agent.getMap().getNeighbors(loc, Map.SMELL_RANGE - 1, Map.SMELL_RANGE_TYPE)) {
                tile.setSmell(false);
            }
            wumpiKilled = true;
        }
        return wumpiKilled;
    }

    @Override
    public final double getFailure() {
        return bazooka.getFailure();
    }

    /**
     * Returns the next available firing location, null if none are available
     * 
     * @return the next available firing location, null if none are available
     */
    public final LocationData getNextFiringLocation() {
        LocationData firingLocation = null;
        if (hasFiringLocation()) {
            firingLocation = firingLocations.poll();
        }
        return firingLocation;
    }

    /**
     * Returns true if this bazooka can fire, false otherwise
     * 
     * @return true if this bazooka can fire, false otherwise
     */
    public final boolean hasAmmo() {
        return bazooka.getAmmo() > 0;
    }

    /**
     * Returns true if this bazooka has a firing location
     * 
     * @return true if this bazooka has a firing location
     */
    public final boolean hasFiringLocation() {
        return ((firingLocations != null) && !firingLocations.isEmpty());
    }

    @Override
    public final void reset() {
        bazooka.reset();
    }

    /**
     * Sets the bazooka's target to the given location
     * 
     * @param loc
     *            the target location
     */
    public final void setTarget(@Name("loc") final LocationData loc) {
        final Map map = agent.getMap();

        firingLocations = new LinkedList<LocationData>();

        // Search all tiles within TARGETING_RANGE for possible firing positions
        for (final Tile candidate : map.getNeighbors(loc, TARGETING_RANGE, TARGETING_RANGE_TYPE)) {
            // Only add safe, navigable tiles that are in line with the target
            final LocationData candidateLoc = candidate.getLocation();
            if (!candidate.hasObstruction() && !candidate.isDangerous() && MapUtils.inLine(candidateLoc, loc)) {
                // Only add firing positions to the list if there are no
                // other obstructions between that position and the target
                boolean canAdd = true;
                for (final Tile between : map.getTilesBetween(candidateLoc, loc)) {
                    if (between.hasObstruction()) {
                        canAdd = false;
                        break;
                    }
                }

                if (canAdd) {
                    firingLocations.add(candidateLoc);
                }
            }
        }
        Collections.sort(firingLocations, new LocationComparator(agent.getMover().getCurrentLocation()));
    }

    /**
     * Returns true if we can fire at a target at the given location
     * 
     * @param targetLocation
     *            the location we want to shoot at
     * @return true if we can fire at a target at the given location
     */
    private final boolean canFire(final LocationData targetLocation) {
        final LocationData currentLocation = agent.getMover().getCurrentLocation();

        boolean canFire = hasAmmo();
        if (canFire) {
            canFire &= MapUtils.inLine(currentLocation, targetLocation);
        }
        if (canFire) {
            canFire &= !agent.getMap().isObstructionBetween(currentLocation, targetLocation);
        }
        if (canFire) {
            canFire &= agent.getMap().getTile(targetLocation).isWumpi();
        }
        return canFire;
    }

    /**
     * The range to use for targeting Wumpis
     */
    private static final int TARGETING_RANGE = Map.SMELL_RANGE * 2;

    /**
     * The type of range to use for targeting Wumpis
     */
    private static final RangeType TARGETING_RANGE_TYPE = RangeType.MANHATTAN;

    /**
     * The agent who owns this capability
     */
    private final AbstractGaaAgent agent;

    /**
     * The agent's bazooka capability
     */
    private final Bazooka bazooka;

    /**
     * This agent's firing locations
     */
    private LinkedList<LocationData> firingLocations;
}
