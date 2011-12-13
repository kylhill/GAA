package edu.ksu.cis.macr.simulator.roles.interpreter;

import edu.ksu.cis.macr.agent.architecture.AssignmentTask;

/**
 * The RoleInterpreter takes a RoleLevelGoalModel (RLGM) and executes it using
 * the information provided by a GoalCapabilityMap (GCM)
 * 
 * @author Kyle Hill
 * 
 */
public interface RoleInterpreter {
    /**
     * Returns the execution status of the goal that this ExecutionPlan is
     * pursuing
     * 
     * @return the execution status of the goal that this ExecutionPlan is
     *         pursuing
     */
    AssignmentTask.Status getGoalStatus();

    /**
     * Returns the RoleLevelGoalModel that backs this RoleInterpreter
     * 
     * @return the RoleLevelGoalModel that backs this RoleInterpreter
     */
    RoleLevelGoalModel getRoleLevelGoalModel();
}
