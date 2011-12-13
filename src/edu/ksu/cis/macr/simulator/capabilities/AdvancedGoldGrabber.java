package edu.ksu.cis.macr.simulator.capabilities;

import edu.ksu.cis.macr.simulator.agent.IExecutionComponent;
import edu.ksu.cis.macr.simulator.agents.AbstractGaaAgent;
import edu.ksu.cis.macr.simulator.capability.AbstractCapabilityAction;
import edu.ksu.cis.macr.simulator.environment.Environment;

/**
 * The AdvancedGoldGrabber capability wraps a GoldGrabber capability internally
 * 
 * @author Kyle Hill
 * 
 */
public class AdvancedGoldGrabber extends AbstractCapabilityAction implements GaaCapability {
    /**
     * Constructs a new AdvancedGoldGrabber capability
     * 
     * @param ownerAgent
     *            the agent who owns this capability
     * @param environment
     *            the environment this capability exists within
     */
    public AdvancedGoldGrabber(final IExecutionComponent ownerAgent, final Environment environment) {
        super(AdvancedGoldGrabber.class, ownerAgent, environment);

        agent = (AbstractGaaAgent) ownerAgent;
        goldGrabber = new GoldGrabber(ownerAgent, environment);
    }

    @Override
    public final double getFailure() {
        return goldGrabber.getFailure();
    }

    /**
     * Grabs gold at the agent's current location
     * 
     * @return true if gold was actually grabbed, false otherwise
     */
    public final boolean grab() {
        final boolean goldGrabbed = goldGrabber.grab();
        if (goldGrabbed) {
            agent.getMap().getTile(agent.getMover().getCurrentLocation()).setGlitter(false);
        }
        return goldGrabbed;
    }

    /**
     * Returns true if this grabber is currently holding gold
     * 
     * @return true if this grabber is currently holding gold
     */
    public final boolean hasGold() {
        return goldGrabber.getAttachedObject() != null;
    }

    /**
     * Places this grabber's gold in the bin
     * 
     * @return true if the gold has actually been placed in the bin, false
     *         otherwise
     */
    public final boolean putGoldIntoBin() {
        return goldGrabber.putGoldIntoBin();
    }

    @Override
    public final void reset() {
        goldGrabber.reset();
    }

    /**
     * The owning agent
     */
    private final AbstractGaaAgent agent;

    /**
     * The agent's GoldGrabber capability
     */
    private final GoldGrabber goldGrabber;
}
