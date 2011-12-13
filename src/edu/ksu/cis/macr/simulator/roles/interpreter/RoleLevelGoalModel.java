package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.util.Set;

import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.SpecificationGoal;

/**
 * The RoleInterpreter takes a RoleLevelGoalModel (RLGM) and executes it using
 * the information provided by a GoalCapabilityMap (GCM)
 * 
 * @author Kyle Hill
 * 
 */
public interface RoleLevelGoalModel {
    /**
     * Indicates that we have failed to achieve our current goal.
     * 
     * @author Kyle Hill
     * 
     */
    class GoalFailureException extends Exception {
        /**
         * Default serial version ID.
         */
        private static final long serialVersionUID = 1L;
    }

    /**
     * Status of the top-level goal this RLGM represents
     * 
     * @author Kyle Hill
     * 
     */
    enum Status {
        /**
         * The goal was achieved
         */
        ACHIEVED,

        /**
         * The goal is currently active (in progress)
         */
        ACTIVE,

        /**
         * The goal could not be achieved
         */
        FAILED
    }

    /**
     * Fires the given event, from the given triggering goal, with the given
     * parameters.
     * 
     * @param triggeringInstanceGoal
     *            the triggering goal
     * @param event
     *            the event to fire
     * @param parameters
     *            the event's parameters
     * @return the set of changes this event caused in the instance tree
     */
    InstanceTreeChanges event(final InstanceGoal<InstanceParameters> triggeringInstanceGoal, final SpecificationEvent event, final InstanceParameters parameters);

    /**
     * Gets the set of events to fire after completion of the given goal. If the
     * given returnValue is a Boolean, then conditional event firing will occur:
     * If returnValue is true, then only the events that start with "true" are
     * returned. If the returnValue is false, then only the events that start
     * with "false" are returned. If no events named "true" or "false" are
     * found, then all events will be returned
     * 
     * @param goal
     *            the goal that was just completed
     * @param returnValue
     *            the optional return value from the previous goal invocation
     * @return the set of events that are ready to be fired
     * @throws GoalFailureException
     *             if it was determined, by the given returnValue, that the
     *             instance goal failed during execution.
     */
    Set<SpecificationEvent> getEventsToFire(final SpecificationGoal goal, final Object returnValue) throws GoalFailureException;

    /**
     * Returns the next goal to execute from the set of active instance goals.
     * If more than one goal is active, then one will be returned
     * non-deterministically.
     * 
     * @return the next goal to execute from the set of active instance goals.
     */
    InstanceGoal<InstanceParameters> getNextInstanceGoal();

    /**
     * Returns the status of the root goal.
     * 
     * @return the status of the root goal.
     */
    Status getRootGoalStatus();

    /**
     * Returns true if there are still active goals to pursue in the RLGM, false
     * otherwise
     * 
     * @return true if there are still active goals to pursue in the RLGM, false
     *         otherwise
     */
    boolean hasActiveInstanceGoals();

    /**
     * Reset the RLGM's instance tree
     * 
     * @param topLevelParams
     *            the parameters to pass to the root goal
     */
    void reset(final InstanceParameters topLevelParams);
}
