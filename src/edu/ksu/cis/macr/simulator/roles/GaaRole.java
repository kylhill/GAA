package edu.ksu.cis.macr.simulator.roles;

import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModel;

/**
 * Generic interface for a GaaRole
 * 
 * @author Kyle Hill
 * 
 */
public interface GaaRole {
    /**
     * Returns the RoleLevelGoalModel associated with this role
     * 
     * @param rootGoal
     *            the root goal of the RoleLevelGoalModel
     * @return the RoleLevelGoalModel associated with this role
     * 
     */
    RoleLevelGoalModel getRoleLevelGoalModel(UniqueIdentifier rootGoal);
}
