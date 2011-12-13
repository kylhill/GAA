package edu.ksu.cis.macr.simulator.capabilities;

import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.agent.IExecutionComponent;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.roles.interpreter.Name;

/**
 * GaaCapabilityImpl decorator for a CommunicationImpl
 * 
 * @author Kyle Hill
 * 
 */
public class AdvancedCommunication extends CommunicationImpl implements GaaCapability {
    /**
     * Constructs a new AdvancedCommunication capability
     * 
     * @param executionComponent
     *            the agent that owns this capability
     * @param environment
     *            the environment this capability runs in
     * @param range
     *            the range of this capability
     * @param sendFailure
     *            the send failure rate
     * @param receiveFailure
     *            the receive failure rate
     */
    public AdvancedCommunication(final IExecutionComponent executionComponent, final Environment environment, final int range, final double sendFailure, final double receiveFailure) {
        super(AdvancedCommunication.class, executionComponent, environment, range, sendFailure, receiveFailure);
    }

    @Override
    public final boolean addChannel(@Name("channelID") final String channelID, @Name("channel") final CommunicationChannel channel) {
        return super.addChannel(channelID, channel);
    }

    @Override
    public final boolean broadcast(@Name("channelID") final String channelID, @Name("content") final Object content) {
        return super.broadcast(channelID, content);
    }

    @Override
    public final boolean broadcastIncludeSelf(@Name("channelID") final String channelID, @Name("content") final Object content) {
        return super.broadcastIncludeSelf(channelID, content);
    }

    @Override
    public final boolean removeChannel(@Name("channelID") final String channelID) {
        return super.removeChannel(channelID);
    }

    @Override
    public final boolean replaceChannel(@Name("channelID") final String channelID, @Name("channel") final CommunicationChannel channel) {
        return super.replaceChannel(channelID, channel);
    }

    @Override
    public final boolean send(@Name("agentID") final UniqueIdentifier agentID, @Name("channelID") final String channelID, @Name("content") final Object content) {
        return super.send(agentID, channelID, content);
    }
}
