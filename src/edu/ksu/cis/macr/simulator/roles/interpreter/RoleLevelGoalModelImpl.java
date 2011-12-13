package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.ksu.cis.macr.goal.model.GoalTree;
import edu.ksu.cis.macr.goal.model.GoalTreeImpl;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.InstanceTreeChanges;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.SpecificationGoal;

/**
 * The RoleLevelGoalModel (RLGM) represents a GMoDS specification and instance
 * tree that models the execution plan for a given role.
 * 
 * @author Kyle Hill
 * 
 */
public class RoleLevelGoalModelImpl implements RoleLevelGoalModel {
    /**
     * Constructs a new RLGM from the given GoalModel with the given root
     * element
     * 
     * @param file
     *            the file containing the GoalModel
     * @param root
     *            the root element of the tree
     */
    public RoleLevelGoalModelImpl(final File file, final String root) {
        goalTree = new GoalTreeImpl(file, root);
    }

    @Override
    public final InstanceTreeChanges event(final InstanceGoal<InstanceParameters> triggeringInstanceGoal, final SpecificationEvent event, final InstanceParameters parameters) {
        return goalTree.event(triggeringInstanceGoal, event, parameters);
    }

    @Override
    public final Set<SpecificationEvent> getEventsToFire(final SpecificationGoal goal, final Object returnValue) throws GoalFailureException {
        final Set<SpecificationEvent> events = goalTree.getSpecificationEvents(goal.getIdentifier());

        // Handle conditional triggers events
        if ((returnValue instanceof Boolean) && !events.isEmpty()) {
            final boolean value = ((Boolean) returnValue).booleanValue();

            String toFire = Boolean.FALSE.toString();
            String toSkip = Boolean.TRUE.toString();
            if (value) {
                toFire = Boolean.TRUE.toString();
                toSkip = Boolean.FALSE.toString();
            }

            final Set<SpecificationEvent> firedEvents = new HashSet<SpecificationEvent>();
            final Set<SpecificationEvent> unconditionalEvents = new HashSet<SpecificationEvent>();

            // Partition the set of specification events into three sets: Events
            // that match the boolean condition, Events that are of the opposite
            // condition, and unconditional events
            for (final SpecificationEvent event : events) {
                final String eventName = event.getIdentifier().toString().toLowerCase();
                if (eventName.startsWith(toFire)) {
                    firedEvents.add(event);
                } else if (!eventName.startsWith(toSkip)) {
                    unconditionalEvents.add(event);
                }
            }

            if (firedEvents.isEmpty()) {
                if (unconditionalEvents.isEmpty()) {
                    if (!value) {
                        // By convention, if a boolean-returning goal returns
                        // false and does not cause any event, then we have
                        // failed to achieve that goal.
                        throw new GoalFailureException();
                    }
                } else {
                    // If we don't have any events that match the conditional
                    // type, then simply fire all unconditional events
                    firedEvents.addAll(unconditionalEvents);
                }
            }
            return firedEvents;
        }

        // Trigger all specification events that start with this goal
        return events;
    }

    @Override
    public final InstanceGoal<InstanceParameters> getNextInstanceGoal() {
        final Iterator<InstanceGoal<InstanceParameters>> iter = goalTree.getActiveInstanceGoals().iterator();

        // Return the first active leaf goal we find
        InstanceGoal<InstanceParameters> goal = null;
        while (iter.hasNext() && (goal == null)) {
            goal = iter.next();
            if (!goalTree.isLeafSpecificationGoal(goal.getSpecificationGoal().getIdentifier())) {
                goal = null;
            }
        }
        return goal;
    }

    @Override
    public final Status getRootGoalStatus() {
        Status status = Status.FAILED;

        final InstanceGoal<InstanceParameters> rootGoal = goalTree.getRootInstanceGoal();
        if (goalTree.getAchievedInstanceGoals().contains(rootGoal)) {
            status = Status.ACHIEVED;
        } else if (goalTree.getActiveInstanceGoals().contains(rootGoal)) {
            status = Status.ACTIVE;
        } else if (goalTree.getFailedInstanceGoals().contains(rootGoal)) {
            status = Status.FAILED;
        } else {
            // The root goal can only be ACTIVE, ACHIEVED, or FAILED
            assert false;
        }
        return status;
    }

    @Override
    public final boolean hasActiveInstanceGoals() {
        return getNextInstanceGoal() != null;
    }

    @Override
    public final void reset(final InstanceParameters topLevelParams) {
        // Clear the instance tree
        goalTree.resetInstanceTree();

        // Create the initial goals
        goalTree.initialize(topLevelParams);
    }

    /**
     * The RLGM's internal GoalTree (both specification and instance tree)
     */
    private final GoalTree goalTree;
}
