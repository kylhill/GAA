package edu.ksu.cis.macr.simulator.agents;

import edu.ksu.cis.macr.simulator.roles.interpreter.GoalCapabilityMap;

/**
 * The generic interface for a GaaAgent
 * 
 * @author Kyle Hill
 * 
 */
public interface GaaAgent {
    /**
     * Returns the GoalCapabilityMap associated with this agent
     * 
     * @return the GoalCapabilityMap associated with this agent
     */
    GoalCapabilityMap getGoalCapabilityMap();
}
