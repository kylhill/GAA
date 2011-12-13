package edu.ksu.cis.macr.simulator.capabilities;

import java.util.HashMap;

import edu.ksu.cis.macr.agent.architecture.AssignmentTask;
import edu.ksu.cis.macr.goal.model.InstanceParameters;
import edu.ksu.cis.macr.organization.model.Assignment;
import edu.ksu.cis.macr.organization.model.InstanceGoal;
import edu.ksu.cis.macr.organization.model.identifiers.StringIdentifier;
import edu.ksu.cis.macr.organization.model.identifiers.UniqueIdentifier;
import edu.ksu.cis.macr.simulator.agent.IExecutionComponent;
import edu.ksu.cis.macr.simulator.agents.AbstractGaaAgent;
import edu.ksu.cis.macr.simulator.capabilities.map.MapUtils;
import edu.ksu.cis.macr.simulator.capability.AbstractCapabilityAction;
import edu.ksu.cis.macr.simulator.capability.Failure;
import edu.ksu.cis.macr.simulator.environment.Environment;
import edu.ksu.cis.macr.simulator.goals.GoalParameters;
import edu.ksu.cis.macr.simulator.goals.ReturnGold;
import edu.ksu.cis.macr.simulator.roles.GoldReturnerRole;

/**
 * A generic OmacsInterface capability for GAA agents
 * 
 * @author Kyle Hill
 * 
 */
public class OmacsInterface extends AbstractCapabilityAction implements GaaCapability {
    /**
     * Constructs a new OmacsInterface
     * 
     * @param ownerAgent
     *            the owning agent
     * @param environment
     *            the capability's environment
     */
    public OmacsInterface(final IExecutionComponent ownerAgent, final Environment environment) {
        super(OmacsInterface.class, ownerAgent, environment);

        agent = (AbstractGaaAgent) ownerAgent;
    }

    /**
     * Creates a new ReturnGoldGoal assignment task and assigns it to the
     * calling agent
     */
    public final void createReturnGoldGoal() {
        // Instantiate a new ReturnGold goal
        final ReturnGold returnGold = new ReturnGold();
        final GoldReturnerRole goldReturnerRole = new GoldReturnerRole();
        final LocationData loc = MapUtils.getGoldReturnLocation();

        final java.util.Map<UniqueIdentifier, Object> paramMap = new HashMap<UniqueIdentifier, Object>();
        paramMap.put(GoalParameters.LOCATION_DATA, loc);

        // Add a new assignment task to fetch the newly-found gold
        final InstanceGoal<InstanceParameters> goal = returnGold.getInstanceGoal(returnGold, StringIdentifier.getIdentifier(returnGold.getIdentifier().toString() + loc.toString()), new InstanceParameters(paramMap));
        agent.addAssignmentTask(new AssignmentTask(new Assignment(agent.getAgentIdentifier(), goldReturnerRole, goal)));
    }

    @Override
    public final double getFailure() {
        return Failure.MIN_FAILURE;
    }

    @Override
    public final void reset() {
        // EMPTY
    }

    /**
     * This capability's owning agent
     */
    private final AbstractGaaAgent agent;
}
