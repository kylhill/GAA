package edu.ksu.cis.macr.simulator.roles;

import edu.ksu.cis.macr.agent.architecture.ExecutionPlan;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.agent.AbstractRolePlan;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleInterpreterImpl;
import edu.ksu.cis.macr.simulator.roles.interpreter.RoleLevelGoalModel;

/**
 * An AbtractRolePlan that provides priority information relative to other plans
 * 
 * @author Kyle Hill
 * 
 */
public abstract class AbstractGaaRole extends AbstractRolePlan implements GaaRole {
    /**
     * Constructs a new instance of <code>AbstractRolePlan</code>.
     * 
     * @param identifier
     *            the <code>UniqueIdentifier</code> of the
     *            <code>AbstractRolePlan</code>.
     */
    protected AbstractGaaRole(final UniqueIdentifier identifier) {
        super(identifier);
    }

    @Override
    public final ExecutionPlan getPlan(final InstanceGoal<?> goal) {
        final RoleLevelGoalModel rlgm = getRoleLevelGoalModel(goal.getSpecificationIdentifier());
        rlgm.reset((InstanceParameters) goal.getParameter());
        return new RoleInterpreterImpl(rlgm);
    }

    /**
     * Returns the relative priority of this plan to other plans
     * 
     * @return the relative priority of this plan to other plans
     */
    public abstract int getPriority();
}
