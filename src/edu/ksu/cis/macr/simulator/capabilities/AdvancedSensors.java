package edu.ksu.cis.macr.simulator.capabilities;

import java.io.Serializable;

import edu.ksu.cis.macr.simulator.agent.IExecutionComponent;
import edu.ksu.cis.macr.simulator.agents.AbstractGaaAgent;
import edu.ksu.cis.macr.simulator.capabilities.map.Map;
import edu.ksu.cis.macr.simulator.capabilities.map.Tile;
import edu.ksu.cis.macr.simulator.capability.AbstractCapabilityAction;
import edu.ksu.cis.macr.simulator.capability.Failure;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.roles.interpreter.Name;

/**
 * The AdvancedSensors capability aggregates the set of sensors required to
 * search the map for gold and Wumpis.
 * 
 * @author Kyle Hill
 * 
 */
public class AdvancedSensors extends AbstractCapabilityAction implements GaaCapability {
    /**
     * Message that is passed between agents for coordination purposes
     * 
     * @author Kyle Hill
     * 
     */
    private static final class Message implements Serializable {
        /**
         * The type of mesage this is
         * 
         * @author Kyle Hill
         * 
         */
        public static enum MessageType {
            /**
             * The message sent is a map
             */
            MAP
        }

        /**
         * Constructs a message of the given type with the given contents
         * 
         * @param t
         *            type of message
         * @param c
         *            message contents
         */
        public Message(final MessageType t, final Object c) {
            type = t;
            contents = c;
        }

        /**
         * Returns the message contents
         * 
         * @return the message contents
         */
        public Object getContents() {
            return contents;
        }

        /**
         * Returns the message type
         * 
         * @return the message type
         */
        public MessageType getType() {
            return type;
        }

        /**
         * The serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        /**
         * The message contents
         */
        private final Object contents;

        /**
         * The message type
         */
        private final MessageType type;
    }

    /**
     * Constructs a new AdvancedSensors capability
     * 
     * @param ownerAgent
     *            the owning agent
     * @param environment
     *            the environment
     */
    public AdvancedSensors(final IExecutionComponent ownerAgent, final Environment environment) {
        super(AdvancedSensors.class, ownerAgent, environment);

        agent = (AbstractGaaAgent) ownerAgent;

        sonar = new RobotSonar(agent, environment);
        sparkleSensor = new SparkleSensor(agent, environment);
        smell = new SmellSensor(agent, environment);
        breeze = new BreezeSensor(agent, environment);
    }

    @Override
    public final double getFailure() {
        return Failure.MIN_FAILURE;
    }

    /**
     * Returns true if we think that gold is present at the given location
     * 
     * @param loc
     *            the location to check for gold
     * @return true if we think that gold is present at the given location
     */
    public final boolean isGoldAtLocation(@Name("loc") final LocationData loc) {
        return agent.getMap().getTile(loc).hasGlitter();
    }

    /**
     * Returns true if the given location is searchable, false otherwise
     * 
     * @param loc
     *            the location whose searchability is to be determined
     * @return true if the given location is searchable, false otherwise
     */
    public final boolean isLocationSearchable(@Name("loc") final LocationData loc) {
        final Tile destTile = agent.getMap().getTile(loc);
        return (!destTile.hasSearched() && !destTile.hasObstruction() && !destTile.isDangerous());
    }

    /**
     * Returns true if we think there is a wumpi present at the given location
     * 
     * @param loc
     *            the location to check for a wumpi
     * @return true if we think there is a wumpi present at the given location
     */
    public final boolean isWumpiAtLocation(@Name("loc") final LocationData loc) {
        return agent.getMap().getTile(loc).isWumpi();
    }

    /**
     * Read all incoming messages from other agents
     */
    public final void readOthersMaps() {
        for (Message message = (Message) agent.receive(); message != null; message = (Message) agent.receive()) {
            switch (message.getType()) {
                case MAP:
                    agent.getMap().mergeFrom((Map) message.getContents());
                    break;

                default:
                    assert false;
                    break;
            }
        }
    }

    /**
     * Read data from sensors and add to the map. Update visited and searched
     * locations.
     * 
     * @return true if any tiles have been updated by our sensor data, false
     *         otherwise
     */
    public final boolean readSensors() {
        final LocationData currentLocation = agent.getMover().getCurrentLocation();
        final LocationData oldLocation = agent.getMover().getOldLocation();
        final Map map = agent.getMap();

        // Update visited locations
        boolean updated = map.updateVisited(currentLocation);

        // Do we need to use the sonar?
        for (final Tile tile : map.getNeighbors(currentLocation, Map.SONAR_RANGE, Map.SONAR_RANGE_TYPE)) {
            if (!tile.hasObstruction() && !tile.hasSearched() && !tile.hasVisited()) {
                updated |= map.updateObstructions(currentLocation, sonar.sense());
                break;
            }
        }

        // Do we need to use the smell sensor?
        for (final Tile tile : map.getNeighbors(currentLocation, Map.SMELL_RANGE, Map.SMELL_RANGE_TYPE)) {
            if (tile.hasSmell() || (!tile.hasObstruction() && !tile.hasSearched())) {
                updated |= map.updateSmell(oldLocation, currentLocation, smell.sense());
                break;
            }
        }

        // Do we need to use the breeze sensor?
        for (final Tile tile : map.getNeighbors(currentLocation, Map.BREEZE_RANGE, Map.BREEZE_RANGE_TYPE)) {
            if (tile.hasBreeze() || (!tile.hasObstruction() && !tile.hasSearched())) {
                updated |= map.updateBreeze(oldLocation, currentLocation, breeze.sense());
                break;
            }
        }

        // Do we need to use the glitter sensor?
        for (final Tile tile : map.getNeighbors(currentLocation, Map.GLITTER_RANGE, Map.GLITTER_RANGE_TYPE)) {
            if (tile.hasGlitter() || (!tile.hasObstruction() && !tile.hasSearched() && !tile.isDangerous())) {
                updated |= map.updateGlitter(oldLocation, currentLocation, sparkleSensor.sense());
                break;
            }
        }

        // Update the searched locations near us
        updated |= map.updateSearched(currentLocation);

        readOthersMaps();
        if (updated) {
            sendOthersMaps();
        }

        return updated;
    }

    @Override
    public final void reset() {
        sonar.reset();
        sparkleSensor.reset();
        smell.reset();
        breeze.reset();
    }

    /**
     * Send this agent's map to other agents
     */
    public final void sendOthersMaps() {
        agent.broadcast(null, new Message(Message.MessageType.MAP, agent.getMap()));
    }

    /**
     * Indicates that the given location is not searchable
     * 
     * @param loc
     *            the location that is not searchable
     */
    public final void setLocationUnsearchable(@Name("loc") final LocationData loc) {
        final Map map = agent.getMap();
        if (!map.isSurrounded(agent.getMover().getCurrentLocation()) && !map.getTile(loc).hasVisited() && isLocationSearchable(loc)) {
            map.getTile(loc).setSearched(false);
            map.getTile(loc).setObstruction(true);
        }
    }

    /**
     * The agent who owns this capability
     */
    private final AbstractGaaAgent agent;

    /**
     * The agent's breeze sensor capability
     */
    private final BreezeSensor breeze;

    /**
     * The agent's smell sensor capability
     */
    private final SmellSensor smell;

    /**
     * The agent's sonar capability
     */
    private final RobotSonar sonar;

    /**
     * The agent's sparkle sensor capability
     */
    private final SparkleSensor sparkleSensor;
}
