package edu.ksu.cis.macr.simulator.roles.interpreter;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import edu.ksu.cis.macr.agent.architecture.AssignmentTask;
import edu.ksu.cis.macr.agent.architecture.ExecutionComponent;
import edu.ksu.cis.macr.agent.architecture.ExecutionPlan;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.goal.model.SpecificationEvent;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.agents.GaaAgent;

/**
 * The RoleInterpreter takes a RoleLevelGoalModel (RLGM) and executes it using
 * the information provided by a GoalCapabilityMap (GCM)
 * 
 * @author Kyle Hill
 * 
 */
public class RoleInterpreterImpl implements RoleInterpreter, ExecutionPlan {
    /**
     * Constructs a new RoleInterpreter using the given RLGM
     * 
     * @param r
     *            the RLGM
     */
    public RoleInterpreterImpl(final RoleLevelGoalModel r) {
        rlgm = r;
    }

    @Override
    public final void execute(final ExecutionComponent executionComponent, final InstanceGoal<?> topLevelGoal) {
        // Get the next leaf-level goal to invoke
        final InstanceGoal<InstanceParameters> instanceGoal = rlgm.getNextInstanceGoal();
        if (instanceGoal != null) {
            final GaaAgent agent = (GaaAgent) executionComponent;

            // Invoke the goal
            Object returnValue = null;
            try {
                returnValue = agent.getGoalCapabilityMap().invoke(instanceGoal);

            } catch (final NoSuchMethodException e) {
                // We tried to invoke a bad method name. The RLGM we're
                // executing probably needs modification, or our agent does not
                // possess (or register) the desired capability.
                onInvocationFailed(instanceGoal);
                return;
            } catch (final IllegalArgumentException e) {
                // We were unable to correctly map the goal's instance
                // parameters to the method's formal parameters. The RLGM we're
                // executing probably needs modification, or our agent does not
                // possess (or register) the desired capability.
                onInvocationFailed(instanceGoal);
                return;
            } catch (final IllegalAccessException e) {
                // We tried to access a method that we are not allowed to.
                onInvocationFailed(instanceGoal);
                return;
            } catch (final InvocationTargetException e) {
                // An assertion in the target method was hit, or a runtime
                // exception was thrown.
                onInvocationFailed(instanceGoal);
                return;
            }

            try {
                // Trigger the appropriate instance tree changes based on
                // this goal's completion
                final InstanceParameters instanceParameters = instanceGoal.getParameter();

                for (final SpecificationEvent event : rlgm.getEventsToFire(instanceGoal.getSpecificationGoal(), returnValue)) {
                    InstanceParameters actualParameters = null;

                    // Map event formal parameters to actual parameters
                    if (event.getParameters() != null) {
                        final Set<UniqueIdentifier> formalParameters = event.getParameters().getParameters();
                        if (!formalParameters.isEmpty()) {
                            actualParameters = event.createInstanceParameters();

                            if (instanceParameters != null) {
                                boolean returnMapped = false;
                                for (final UniqueIdentifier formalParameter : formalParameters) {
                                    final Object actualParameter = instanceParameters.getValue(formalParameter);
                                    if (actualParameter != null) {
                                        actualParameters.setValue(formalParameter, actualParameter);
                                    } else {
                                        if (!returnMapped) {
                                            actualParameters.setValue(formalParameter, returnValue);
                                            returnMapped = true;
                                        } else {
                                            // We can only pass along the return
                                            // value
                                            assert false;
                                        }
                                    }
                                }
                            } else {
                                // We can only pass along the return value
                                assert formalParameters.size() == 1;
                                actualParameters.setValue(formalParameters.iterator().next(), returnValue);
                            }
                        }
                    }

                    // Signal the event
                    rlgm.event(instanceGoal, event, actualParameters);
                }

                // Signal the we achieved the goal
                rlgm.event(instanceGoal, SpecificationEvent.ACHIEVED_EVENT, null);

            } catch (final RoleLevelGoalModel.GoalFailureException e) {
                // Signal the we failed the goal
                rlgm.event(instanceGoal, SpecificationEvent.FAILED_EVENT, null);
            }
        }
    }

    @Override
    public final AssignmentTask.Status getGoalStatus() {
        switch (rlgm.getRootGoalStatus()) {
            case ACHIEVED:
                return AssignmentTask.Status.ACHIEVED;

            case FAILED:
                return AssignmentTask.Status.FAILED;

            case ACTIVE:
                return AssignmentTask.Status.IN_PROGRESS;

            default:
                assert false;
                return AssignmentTask.Status.IN_PROGRESS;
        }
    }

    @Override
    public final RoleLevelGoalModel getRoleLevelGoalModel() {
        return rlgm;
    }

    @Override
    public final boolean isDone() {
        return !rlgm.hasActiveInstanceGoals();
    }

    @Override
    public final boolean isPreemptible(final ExecutionComponent executionComponent) {
        return false;
    }

    /**
     * Called whenever method invocation fails for any reason
     * 
     * @param goal
     *            the InstanceGol who's invocation failed
     */
    private final void onInvocationFailed(final InstanceGoal<InstanceParameters> goal) {
        assert false;
        rlgm.event(goal, SpecificationEvent.FAILED_EVENT, null);
    }

    /**
     * The RLGM we're interpreting
     */
    private final RoleLevelGoalModel rlgm;
}
